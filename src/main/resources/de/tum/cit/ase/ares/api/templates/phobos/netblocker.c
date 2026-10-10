#define _GNU_SOURCE

#include <dlfcn.h>

#include <stdio.h>

#include <stdlib.h>

#include <string.h>

#include <strings.h>

#include <errno.h>

#include <pthread.h>

#include <arpa/inet.h>

#include <netdb.h>


#include <stdint.h>

#include <sys/socket.h>

/*=======================  Rule table  =======================*/

typedef struct rule {
  char * host; /* literal, wildcard, or "*"             */
  struct in6_addr net6; /* v6‑mapped network base for CIDR rules  */
  int cidr_bits; /* 0 ⇒ host/IP rule                       */
  unsigned short port; /* 0 ⇒ any port                           */
  struct rule * next;
} rule_t;

static rule_t * rules = NULL;
static pthread_rwlock_t rules_lock = PTHREAD_RWLOCK_INITIALIZER;

/*=======================  <IP,port> cache  ==================*/

/* One authorisation for a resolved address. An entry always carries the port
   authorisation a policy rule granted: either a single port, or any_port for a
   rule that deliberately permits every port. The port a name lookup happens to
   report is never an authorisation in itself. */
typedef struct ip_node {
  char ip[INET6_ADDRSTRLEN];
  unsigned short port;
  int any_port;
  struct ip_node * next;
} ip_t;
static ip_t * ip_cache = NULL;
static size_t ip_cache_sz = 0;
static const size_t IP_CACHE_MAX = 1024;
static pthread_mutex_t ip_lock = PTHREAD_MUTEX_INITIALIZER;

static int ip_cache_contains(const char *ip, unsigned short port)
{
    pthread_mutex_lock(&ip_lock);
    for (ip_t *n = ip_cache; n; n = n->next) {
        if (!strcasecmp(n->ip, ip) &&
            (n->any_port || n->port == port)) {
            pthread_mutex_unlock(&ip_lock);
            return 1;
        }
    }
    pthread_mutex_unlock(&ip_lock);
    return 0;
}

/* Exact-entry lookup used before inserting, so that an any-port authorisation
   and a single-port authorisation for the same address stay distinguishable
   instead of one hiding the other. */
static int ip_cache_has_entry(const char *ip, unsigned short port, int any_port)
{
    pthread_mutex_lock(&ip_lock);
    for (ip_t *n = ip_cache; n; n = n->next) {
        if (!strcasecmp(n->ip, ip) && n->any_port == any_port && n->port == port) {
            pthread_mutex_unlock(&ip_lock);
            return 1;
        }
    }
    pthread_mutex_unlock(&ip_lock);
    return 0;
}

static void ip_cache_insert(const char * ip, unsigned short port, int any_port) {
  if (ip_cache_has_entry(ip, port, any_port)) return;
  ip_t * n = calloc(1, sizeof * n);
  /* An address without an entry is re-evaluated against the rules, so a failed
     allocation withholds an authorisation rather than granting one. */
  if (!n) return;
  strncpy(n -> ip, ip, sizeof n -> ip - 1);
  n -> port = port;
  n -> any_port = any_port;
  pthread_mutex_lock( & ip_lock);
  n -> next = ip_cache;
  ip_cache = n;
  if (++ip_cache_sz > IP_CACHE_MAX) {
    ip_t * cur = ip_cache, * prev = NULL;
    while (cur && cur -> next) {
      prev = cur;
      cur = cur -> next;
    }
    if (prev) {
      free(cur);
      prev -> next = NULL;
      ip_cache_sz--;
    }
  }
  pthread_mutex_unlock( & ip_lock);
}

static void ip_cache_clear(void) {
  pthread_mutex_lock( & ip_lock);
  while (ip_cache) {
    ip_t * n = ip_cache -> next;
    free(ip_cache);
    ip_cache = n;
  }
  ip_cache_sz = 0;
  pthread_mutex_unlock( & ip_lock);
}

/*=======================  Helpers  ==========================*/

static int to_canon(const char * src, char * canon, struct in6_addr * out6) {
  if (inet_pton(AF_INET6, src, out6) == 1) {
    if (canon) {
      strncpy(canon, src, INET6_ADDRSTRLEN - 1);
      canon[INET6_ADDRSTRLEN - 1] = '\0';
    }
    return 0;
  }
  struct in_addr v4;
  if (inet_pton(AF_INET, src, & v4) == 1) {
    memset(out6, 0, sizeof * out6);
    out6 -> s6_addr[10] = 0xff;
    out6 -> s6_addr[11] = 0xff;
    memcpy( & out6 -> s6_addr[12], & v4, 4);
    if (canon) inet_ntop(AF_INET, & v4, canon, INET6_ADDRSTRLEN);
    return 0;
  }
  return -1;
}

static int cidr_match(const struct in6_addr * addr,
  const struct in6_addr * net, int bits) {
  if (bits <= 0 || bits > 128) return 0;
  int full = bits / 8, rem = bits % 8;
  if (memcmp(addr, net, full) != 0) return 0;
  if (!rem) return 1;
  uint8_t mask = ~((1 << (8 - rem)) - 1);
  return ((addr -> s6_addr[full] & mask) == (net -> s6_addr[full] & mask));
}

/*=======================  Rule loading  =====================*/

static void free_rules(void) {
  while (rules) {
    rule_t * n = rules -> next;
    free(rules -> host);
    free(rules);
    rules = n;
  }
}

//TODO: we should supply allow list as an argument to the binary instead of env var NETBLOCKER_CONF
static void load_rules_inner(void) {
  const char * cfg = getenv("NETBLOCKER_CONF");
  if (!cfg) return;
  FILE * f = fopen(cfg, "r");
  if (!f) return;
  char line[512];
  while (fgets(line, sizeof line, f)) {
    char * hash = strchr(line, '#');
    if (hash) * hash = '\0';
    char * tok = strtok(line, " \t\r\n");
    if (!tok) continue;
    char * porttok = strtok(NULL, " \t\r\n");
    unsigned short port = 0;
    if (porttok && strcmp(porttok, "*")) {
      char * end;
      unsigned long p = strtoul(porttok, & end, 10);
      if ( * end || p > 65535) continue;
      port = (unsigned short) p;
    }
    int cidr = 0;
    struct in6_addr net6;
    char * slash = strchr(tok, '/');
    if (slash) {
      * slash = '\0';
      char * end;
      unsigned long bits = strtoul(slash + 1, & end, 10);
      if ( * end || bits == 0 || bits > 128) continue;
      cidr = (int) bits;
      if (to_canon(tok, NULL, & net6) != 0) continue;
    }
    rule_t * r = calloc(1, sizeof * r);
    /* Out of memory. Skipping the rule leaves the host it names out of the allow-list, so
       that host is refused rather than permitted. For a sandbox that is the safe
       direction, and it beats dereferencing NULL inside a library preloaded into every
       process a graded run starts. */
    if (!r) continue;
    r -> host = strdup(tok);
    /* Same reasoning, plus r would leak. Every reader of host dereferences it without a
       NULL check (see host_match and the connect hook), so a rule whose host failed to
       allocate must never enter the list. */
    if (!r -> host) {
      free(r);
      continue;
    }
    r -> cidr_bits = cidr;
    r -> port = port;
    if (cidr) r -> net6 = net6;
    r -> next = rules;
    rules = r;
  }
  fclose(f);
}

static void reload_rules(void) {
  pthread_rwlock_wrlock( & rules_lock);
  free_rules();
  load_rules_inner();
  ip_cache_clear();
  pthread_rwlock_unlock( & rules_lock);
}
/*=======================  Evaluation  =======================*/

static int host_match(const char * h,
  const rule_t * r) {
  if (!strcasecmp(r -> host, "*")) return (r -> port == 0);
  if (r -> host[0] == '*' && r -> host[1] == '.') {
    size_t hl = strlen(h), sl = strlen(r -> host + 1);
    return hl >= sl && !strcasecmp(h + hl - sl, r -> host + 1);
  }
  return !strcasecmp(h, r -> host);
}

static int host_allowed(const char * h, unsigned short port) {
  pthread_rwlock_rdlock( & rules_lock);
  for (rule_t * r = rules; r; r = r -> next) {
    if (r -> cidr_bits) continue;
    if (port == 0 && host_match(h, r)) { pthread_rwlock_unlock(&rules_lock); return 1; }
    if ((!r -> port || r -> port == port || port == 0) && host_match(h, r)) {
      pthread_rwlock_unlock( & rules_lock);
      return 1;
    }
  }
  pthread_rwlock_unlock( & rules_lock);
  return 0;
}

/* Records, against one address a hostname resolved to, the ports the policy
   grants that hostname. A resolver reply carries no service of its own, so the
   authorisation is read from the matching rules; resolving a host can
   therefore never widen a single-port rule into access on every port. */
static void cache_authorised_ports(const char * host,
  const char * ip) {
  pthread_rwlock_rdlock( & rules_lock);
  for (rule_t * r = rules; r; r = r -> next) {
    if (r -> cidr_bits) continue;
    if (!host_match(host, r)) continue;
    ip_cache_insert(ip, r -> port, r -> port == 0);
  }
  pthread_rwlock_unlock( & rules_lock);
}

static int ip_allowed(const char *ip, unsigned short port)
{
    if (ip_cache_contains(ip, port))
        return 1;

    /* normalise caller-supplied IP into v6 canonical form */
    struct in6_addr a6;
    char canon_ip[INET6_ADDRSTRLEN] = "";
    if (to_canon(ip, canon_ip, &a6) != 0)
        return 0;

    pthread_rwlock_rdlock(&rules_lock);

    /* ---------- 1st pass: literal & CIDR rules ---------- */
    for (rule_t *r = rules; r; r = r->next) {      /*  ◀─ r declared here */
        if (r->port && r->port != port)
            continue;

        /* literal host/IP rule (cidr_bits == 0) */
        if (!r->cidr_bits) {
            /* accept wildcard “*” (all hosts) */
            if (!strcasecmp(r->host, "*")) {
                pthread_rwlock_unlock(&rules_lock);
                return 1;
            }

            /* exact-IP rule — compare canonical forms */
            if ((strchr(r->host, '.') || strchr(r->host, ':'))) {
                char canon_rule[INET6_ADDRSTRLEN] = "";
                struct in6_addr rule6;
                if (to_canon(r->host, canon_rule, &rule6) == 0 &&
                    memcmp(&a6, &rule6, sizeof a6) == 0) {
                    pthread_rwlock_unlock(&rules_lock);
                    return 1;
                }
            }
            continue;   /* literal host names handled later */
        }

        /* CIDR rule */
        if (cidr_match(&a6, &r->net6, r->cidr_bits)) {
            pthread_rwlock_unlock(&rules_lock);
            return 1;
        }
    }

    /* ---------- 2nd pass: wildcard host back-fill ---------- */
    for (rule_t *r = rules; r; r = r->next) {
        if (r->cidr_bits ||
            r->host[0] != '*' || r->host[1] != '.' ||
            (r->port && r->port != port))
            continue;

        /* strip leading "*." */
        const char *suffix = r->host + 1;
        struct addrinfo *res = NULL;

        if (getaddrinfo(suffix, NULL, NULL, &res) == 0) {
            for (struct addrinfo *ai = res; ai; ai = ai->ai_next) {
                char buf[INET6_ADDRSTRLEN] = "";
                getnameinfo(ai->ai_addr, ai->ai_addrlen,
                            buf, sizeof buf, NULL, 0, NI_NUMERICHOST);

                /* cache each discovered IP for speed, under the port this rule
                   authorises */
                ip_cache_insert(buf, r->port, r->port == 0);

                if (!strcasecmp(buf, canon_ip)) {
                    freeaddrinfo(res);
                    pthread_rwlock_unlock(&rules_lock);
                    return 1;            /* match found */
                }
            }
            freeaddrinfo(res);
        }
    }

    pthread_rwlock_unlock(&rules_lock);
    return 0;   /* no rule matched */
}


/*=======================  Hooks  ============================*/

typedef int( * gai_f)(const char * ,
  const char * ,
    const struct addrinfo * , struct addrinfo ** );
typedef int( * conn_f)(int,
  const struct sockaddr * , socklen_t);
static gai_f real_gai = NULL;
static conn_f real_conn = NULL;

int getaddrinfo(const char * node,
  const char * svc,
    const struct addrinfo * hints, struct addrinfo ** res) {
  if (!real_gai) real_gai = (gai_f) dlsym(RTLD_NEXT, "getaddrinfo");
  unsigned short svc_port = 0;
  if (svc) {
    char * end;
    unsigned long p = strtoul(svc, & end, 10);
    if (! * end && p <= 65535) svc_port = (unsigned short) p;
  }
  if (node && !host_allowed(node, svc_port)) return EAI_FAIL;
  int rc = real_gai(node, svc, hints, res);
  if (rc == 0 && node && host_allowed(node, svc_port)) {
    for (struct addrinfo * ai = * res; ai; ai = ai -> ai_next) {
      char buf[INET6_ADDRSTRLEN] = "";
      if (getnameinfo(ai -> ai_addr, ai -> ai_addrlen, buf, sizeof buf, NULL, 0, NI_NUMERICHOST) != 0)
        continue;
      cache_authorised_ports(node, buf);
    }
  }
  return rc;
}

int connect(int fd,
  const struct sockaddr * sa, socklen_t len) {
  if (!real_conn) real_conn = (conn_f) dlsym(RTLD_NEXT, "connect");
  char ip[INET6_ADDRSTRLEN] = "";
  unsigned short port = 0;
  if (sa -> sa_family == AF_INET) {
    const struct sockaddr_in * s = (const struct sockaddr_in * ) sa;
    inet_ntop(AF_INET, & s -> sin_addr, ip, sizeof ip);
    port = ntohs(s -> sin_port);
  } else if (sa -> sa_family == AF_INET6) {
    const struct sockaddr_in6 * s6 = (const struct sockaddr_in6 * ) sa;
    inet_ntop(AF_INET6, & s6 -> sin6_addr, ip, sizeof ip);
    port = ntohs(s6 -> sin6_port);
  }
  if (ip_allowed(ip, port)) return real_conn(fd, sa, len);
  errno = EACCES;
  return -1;
}

/* The rule table is read once, here, before the supervised process runs a line of its
   own code. There is deliberately no SIGHUP reload: the file NETBLOCKER_CONF names lives
   under a directory the sandbox binds writable, so a reload would let the code being
   supervised rewrite the allow-list and signal itself into reaching a refused host. The
   table this constructor builds is the one that decides every later call. */
__attribute__((constructor)) static void nb_init(void) {
  reload_rules();
  real_gai = (gai_f) dlsym(RTLD_NEXT, "getaddrinfo");
  real_conn = (conn_f) dlsym(RTLD_NEXT, "connect");
}