#!/usr/bin/env bash
# shellcheck shell=bash
set -euo pipefail
PHB_OK=0
PHB_EPOLICY=11
PHB_EMERGE=12
PHB_EBASE=13
PHB_ETIMEOUT=14
PHB_ERUNTIME=15
_log()   { printf '%s\n' "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] $*" >&2; }
die()    { _log "$1"; exit "${2:-1}"; }
report() { printf '%s\n' "$1"; }
uniq_keep_order() { awk '!seen[$0]++'; }
depth_sort()      { awk '{print gsub(/\//,"/")+1 " " $0}' | sort -k1,1n -k2,2 | cut -d" " -f2-; }
canon_paths() {
  if command -v realpath >/dev/null 2>&1; then
    while IFS= read -r p; do [[ -z "$p" ]] && continue; realpath --canonicalize-missing --no-symlinks "$p" || echo "$p"; done
  else
    cat
  fi
}
allowed_keys() {
  cat <<'EOF'
readonly
read
write
hide
tmpfs
network
limits
timeout
EOF
}
trim_ends() {
  local text="$1"
  text="${text#"${text%%[![:space:]]*}"}"
  printf '%s' "${text%"${text##*[![:space:]]}"}"
}
# A comment runs to the end of its line and the ends are then trimmed. Every
# reader of a configuration goes through this, so no two of them can disagree
# about what a line says before they decide what it means.
preprocess_config_line() { trim_ends "${1%%#*}"; }
# Only the padding around a section name is removed. Internal whitespace is part
# of the name, so '[read only]' names 'read only' rather than 'readonly', and
# section contents and path values are never touched.
normalise_section_name() { trim_ends "$1"; }
# The one grammar for section headers, shared by validation and parsing so a
# header can never be read as one section by one of them and another section, or
# nothing at all, by the other.
#
# A preprocessed line that begins with '[' is a header and is held to the whole
# grammar; anything else is section content, which is what keeps a path that
# merely contains brackets a path. A header must be exactly one bracket pair
# around a name that is not empty once trimmed.
#
# Prints the normalised name and returns 0 for a header, 1 for ordinary content,
# 2 for a header-looking line that is malformed, and 3 for one that names
# nothing.
classify_config_line() {
  local line="$1" name
  [[ "$line" == '['* ]] || return 1
  [[ "$line" =~ ^\[([^][]*)\]$ ]] || return 2
  name="$(normalise_section_name "${BASH_REMATCH[1]}")"
  [[ -n "$name" ]] || return 3
  printf '%s' "$name"
}
# Validation and parsing reject a bad header identically because they classify
# it identically. A header the grammar refuses must never fall through and be
# discarded as though it were section content.
reject_bad_section_header() {
  local status="$1" line="$2"
  case "$status" in
    2) report "Policy invalid: malformed section header: '${line}'. (PHB-EPOLICY)"; exit "${PHB_EPOLICY}" ;;
    3) report "Policy invalid: section header without a name. (PHB-EPOLICY)"; exit "${PHB_EPOLICY}" ;;
  esac
}
validate_config_file_keys() {
  local file="$1"
  local ok unknown="" raw line name status
  ok=$(allowed_keys | sort -u)
  declare -A SEEN_SECTIONS=()
  while IFS= read -r raw || [[ -n "$raw" ]]; do
    line="$(preprocess_config_line "$raw")"
    status=0; name="$(classify_config_line "$line")" || status=$?
    reject_bad_section_header "$status" "$line"
    (( status == 0 )) || continue
    [[ -n "${SEEN_SECTIONS["$name"]:-}" ]] && continue
    SEEN_SECTIONS["$name"]=1
    # -F because the parser compares section names literally: without it a name
    # such as 're.d' would match the allowed 'read' as a pattern and then be
    # parsed as a section nobody handles. -- so a name may begin with a dash.
    if ! grep -Fqx -- "$name" <<< "$ok"; then unknown+="$name "; fi
  done <"$file"
  if [[ -n "$unknown" ]]; then
    report "Policy invalid: unknown key(s): ${unknown}. (PHB-EPOLICY)"
    exit "${PHB_EPOLICY}"
  fi
}
# Records a parsed timeout value, treating a numeric zero (0, 0.0, 0.000) as
# "no timeout". The zero test is textual on purpose: [[ -eq ]] is integer
# arithmetic and aborts with a syntax error on a decimal value.
# PARSED_TIMEOUT_SET reports whether the current file supplied a value at all,
# because "no timeout" and "nothing said about the timeout" resolve to the same
# empty PARSED_TIMEOUT but must layer differently.
set_parsed_timeout() {
  local value="$1"
  if [[ "$value" =~ ^0+(\.0+)?$ ]]; then PARSED_TIMEOUT=""; else PARSED_TIMEOUT="$value"; fi
  PARSED_TIMEOUT_SET=1
}
# A dedicated timeout section must provide exactly one value; validate it when
# leaving the section and at EOF. The count is what decides, not PARSED_TIMEOUT:
# a valid zero is one supplied value even though it resolves to the empty
# "no timeout", and PARSED_TIMEOUT outlives a single file, so an empty section
# would otherwise pass on a previously parsed policy's value.
require_exactly_one_timeout() {
  local section="$1"
  local values="$2"
  [[ "$section" == "timeout" ]] || return 0
  if (( values != 1 )); then
    report "Policy invalid: a [timeout] section must provide exactly one timeout value, but provided ${values}. (PHB-EPOLICY)"
    exit "${PHB_EPOLICY}"
  fi
}
parse_cfg_policy() {
  local cfg="$1"
  validate_config_file_keys "$cfg"
  local tdir; tdir="$(mktemp -d -t phobos-cfg.XXXXXX)"
  INI_TMP_DIRS+=" ${tdir}"
  local ro="${tdir}/ro.paths" rw="${tdir}/rw.paths" hide="${tdir}/hide.paths" net="${tdir}/net.rules"
  : >"$ro"; : >"$rw"; : >"$hide"; : >"$net"
  local sec=""
  local raw line name status
  # Section-local, so it never carries a count from another section or file.
  local timeout_values=0
  # Per-file, so the caller can tell "this policy set no timeout" from "this
  # policy set a timeout of zero".
  PARSED_TIMEOUT_SET=0
  while IFS= read -r raw || [[ -n "$raw" ]]; do
    line="$(preprocess_config_line "$raw")"
    [[ -z "$line" ]] && continue
    status=0; name="$(classify_config_line "$line")" || status=$?
    reject_bad_section_header "$status" "$line"
    if (( status == 0 )); then
      require_exactly_one_timeout "$sec" "$timeout_values"
      sec="$name"; timeout_values=0; continue
    fi
    case "$sec" in
      readonly|read) printf '%s\n' "$line" >>"$ro" ;;
      write)         printf '%s\n' "$line" >>"$rw" ;;
      hide|tmpfs)    printf '%s\n' "$line" >>"$hide" ;;
      network)
        if [[ "$line" =~ ^allow[[:space:]]+(.+)$ ]]; then
          local target="${BASH_REMATCH[1]}" host port="*"
          if [[ "$target" == *:* ]]; then host="${target%:*}"; port="${target##*:}"; else host="$target"; fi
          host="${host#[}"; host="${host%]}"; printf '%s %s\n' "$host" "$port" >>"$net"
        fi ;;
      limits|timeout)
        # The generator writes the timeout as canonical decimal seconds, S.mmm
        # (JavaPhobosTestCase#serialiseLimitValue), so the value is not an
        # integer. An integer-only pattern matched nothing, left PARSED_TIMEOUT
        # empty and thereby disabled the timeout entirely, which is why an
        # unrecognised timeout assignment now fails closed instead.
        if [[ "$line" =~ ^timeout[[:space:]]*=[[:space:]]*([0-9]+(\.[0-9]+)?)$ ]]; then
          set_parsed_timeout "${BASH_REMATCH[1]}"; timeout_values=$(( timeout_values + 1 ))
        elif [[ "$line" =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
          set_parsed_timeout "$line"; timeout_values=$(( timeout_values + 1 ))
        elif [[ "$line" =~ ^timeout([[:space:]:=]|$) || "$sec" == "timeout" ]]; then
          # Every line of a dedicated [timeout] section is the timeout value
          # itself, so anything unreadable there is an error too. A [limits]
          # section may contain unrelated entries. Treat timeout as a
          # declaration only when it ends the line or is followed by whitespace,
          # '=' or ':', so malformed timeout declarations fail closed without
          # policing other keys.
          report "Policy invalid: timeout must be a number of seconds, but was '${line}'. (PHB-EPOLICY)"
          exit "${PHB_EPOLICY}"
        fi ;;
      *) ;;
    esac
  done <"$cfg"
  require_exactly_one_timeout "$sec" "$timeout_values"
  PARSED_RO_FILE="$ro"; PARSED_RW_FILE="$rw"; PARSED_HIDE_FILE="$hide"; PARSED_NET_FILE="$net"; : "${PARSED_TIMEOUT:=}"
}
merge_fs_per_path() {
  local base_ro="$1" base_rw="$2" base_hide="$3"
  local -n cur_ro_ref="$4"; local -n cur_rw_ref="$5"; local -n cur_hide_ref="$6"
  local add_ro="$7" add_rw="$8" add_hide="$9"
  declare -A BASE_RO=() BASE_RW=()
  if [[ -s "$base_ro" ]]; then while IFS= read -r p; do [[ -z "$p" ]] && continue; BASE_RO["$p"]=1; done < <(canon_paths < "$base_ro"); fi
  if [[ -s "$base_rw" ]]; then while IFS= read -r p; do [[ -z "$p" ]] && continue; BASE_RW["$p"]=1; done < <(canon_paths < "$base_rw"); fi
  declare -A CUR_RO=() CUR_RW=() CUR_HIDE=()
  if [[ -s "$cur_ro_ref"  ]]; then while IFS= read -r p; do [[ -z "$p" ]] && continue; CUR_RO["$p"]=1; done < "$cur_ro_ref"; fi
  if [[ -s "$cur_rw_ref"  ]]; then while IFS= read -r p; do [[ -z "$p" ]] && continue; CUR_RW["$p"]=1; done < "$cur_rw_ref"; fi
  if [[ -s "$cur_hide_ref" ]]; then while IFS= read -r p; do [[ -z "$p" ]] && continue; CUR_HIDE["$p"]=1; done < "$cur_hide_ref"; fi
  if [[ -s "$add_hide" ]]; then
    while IFS= read -r p; do [[ -z "$p" ]] && continue; CUR_HIDE["$p"]=1; unset CUR_RO["$p"]; unset CUR_RW["$p"]; done < <(canon_paths < "$add_hide" | uniq_keep_order)
  fi
  if [[ -s "$add_ro" ]]; then
    while IFS= read -r p; do
      [[ -z "$p" ]] && continue
      if [[ -n "${BASE_RO["$p"]:-}" || -n "${BASE_RW["$p"]:-}" ]]; then
        CUR_RO["$p"]=1; unset CUR_RW["$p"]; unset CUR_HIDE["$p"]
      else
        report "Policy merge failed: path '$p' requested RO but base does not allow access. (PHB-EMERGE)"; exit "${PHB_EMERGE}"
      fi
    done < <(canon_paths < "$add_ro" | uniq_keep_order)
  fi
  if [[ -s "$add_rw" ]]; then
    while IFS= read -r p; do
      [[ -z "$p" ]] && continue
      if [[ -n "${BASE_RW["$p"]:-}" ]]; then
        CUR_RW["$p"]=1; unset CUR_RO["$p"]; unset CUR_HIDE["$p"]
      else
        report "Policy merge failed: path '$p' requested RW but base forbids write. (PHB-EMERGE)"; exit "${PHB_EMERGE}"
      fi
    done < <(canon_paths < "$add_rw" | uniq_keep_order)
  fi
  { for k in "${!CUR_RO[@]}"; do echo "$k"; done; } | uniq_keep_order | depth_sort > "$cur_ro_ref" || : > "$cur_ro_ref"
  { for k in "${!CUR_RW[@]}"; do echo "$k"; done; } | uniq_keep_order | depth_sort > "$cur_rw_ref" || : > "$cur_rw_ref"
  { for k in "${!CUR_HIDE[@]}"; do echo "$k"; done; } | uniq_keep_order | depth_sort > "$cur_hide_ref" || : > "$cur_hide_ref"
}
net_union() {
  local out="$1"; shift
  : > "$out"
  declare -A SEEN=()
  for f in "$@"; do
    [[ -n "$f" && -s "$f" ]] || continue
    while IFS= read -r ln; do
      [[ -z "$ln" ]] && continue
      if [[ -z "${SEEN["$ln"]:-}" ]]; then
        echo "$ln" >> "$out"; SEEN["$ln"]=1
      fi
    done < <(sed -E 's/#.*$//' "$f" | sed '/^[[:space:]]*$/d')
  done
}

filter_existing() { while IFS= read -r p; do [[ -n "$p" && -e "$p" ]] && printf '%s\n' "$p"; done; }

write_spec() {
  local spec_dir="$1" ro="$2" rw="$3" hide="$4" net="$5" timeout="$6" tail="$7"
  mkdir -p "$spec_dir"

  if [[ -s "$ro" ]]; then filter_existing < "$ro" > "${spec_dir}/ro.paths"; else : > "${spec_dir}/ro.paths"; fi
  if [[ -s "$hide" ]]; then filter_existing < "$hide" > "${spec_dir}/hide.paths"; else : > "${spec_dir}/hide.paths"; fi

  cp "$rw"   "${spec_dir}/rw.paths"   2>/dev/null || : > "${spec_dir}/rw.paths"

  if [[ -n "$timeout" ]]; then printf '%s\n' "$timeout" > "${spec_dir}/timeout.sec"; else : > "${spec_dir}/timeout.sec"; fi
  if [[ -n "$tail" && -f "$tail" ]]; then sed -E 's/#.*$//' "$tail" | sed '/^[[:space:]]*$/d' > "${spec_dir}/tail.flags"; else : > "${spec_dir}/tail.flags"; fi
  if [[ -n "$net" && -s "$net" ]]; then cp "$net" "${spec_dir}/net.rules"; else : > "${spec_dir}/net.rules"; fi
}
fs_union_files() {
  local out_ro="$1" out_rw="$2" out_hide="$3" in_ro="$4" in_rw="$5" in_hide="$6"
  tmpd="$(mktemp -d)"; trap 'rm -rf "$tmpd"' RETURN
  for k in ro rw hide; do : >"${tmpd}/${k}.all"; done
  [[ -s "$out_ro"   ]] && cat "$out_ro"   >> "${tmpd}/ro.all"
  [[ -s "$out_rw"   ]] && cat "$out_rw"   >> "${tmpd}/rw.all"
  [[ -s "$out_hide" ]] && cat "$out_hide" >> "${tmpd}/hide.all"
  [[ -s "$in_ro"    ]] && cat "$in_ro"    >> "${tmpd}/ro.all"
  [[ -s "$in_rw"    ]] && cat "$in_rw"    >> "${tmpd}/rw.all"
  [[ -s "$in_hide"  ]] && cat "$in_hide"  >> "${tmpd}/hide.all"

  canon_paths < "${tmpd}/ro.all"   | uniq_keep_order | depth_sort > "$out_ro"   || : > "$out_ro"
  canon_paths < "${tmpd}/rw.all"   | uniq_keep_order | depth_sort > "$out_rw"   || : > "$out_rw"
  canon_paths < "${tmpd}/hide.all" | uniq_keep_order | depth_sort > "$out_hide" || : > "$out_hide"
}
