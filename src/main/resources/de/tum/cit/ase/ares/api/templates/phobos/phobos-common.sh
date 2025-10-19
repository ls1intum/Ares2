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
TIMEOUT_SECONDS
NET_ALLOWLIST_FILE
RO_PATHS_FILE
RW_PATHS_FILE
HIDE_PATHS_FILE
TAIL_FLAGS_FILE
EOF
}
validate_config_file_keys() {
  local file="$1"
  local keys ok unknown=""
  keys=$(sed -E -n 's/^[[:space:]]*([A-Za-z_][A-Za-z0-9_]*)=.*/\1/p' "$file" | sed 's/[[:space:]]//g' | sort -u)
  ok=$(allowed_keys | sort -u)
  while IFS= read -r k; do
    [[ -z "$k" ]] && continue
    if ! grep -qx "$k" <<< "$ok"; then unknown+="$k "; fi
  done <<< "$keys"
  if [[ -n "$unknown" ]]; then
    report "Policy invalid: unknown key(s): ${unknown}. (PHB-EPOLICY)"
    exit "${PHB_EPOLICY}"
  fi
}
parse_cfg_policy() {
  local cfg="$1"
  local tdir; tdir="$(mktemp -d -t phobos-cfg.XXXXXX)"
  INI_TMP_DIRS+=" ${tdir}"
  local ro="${tdir}/ro.paths" rw="${tdir}/rw.paths" hide="${tdir}/hide.paths" net="${tdir}/net.rules"
  : >"$ro"; : >"$rw"; : >"$hide"; : >"$net"
  local sec=""
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%%#*}"; line="$(echo "$line" | sed -E 's/^[[:space:]]+//; s/[[:space:]]+$//')"
    [[ -z "$line" ]] && continue
    if [[ "$line" =~ ^\[(.+)\]$ ]]; then sec="${BASH_REMATCH[1]}"; continue; fi
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
        if [[ "$line" =~ ^timeout[[:space:]]*=[[:space:]]*([0-9]+)$ ]]; then
          local t="${BASH_REMATCH[1]}"; [[ "$t" -eq 0 ]] && PARSED_TIMEOUT="" || PARSED_TIMEOUT="$t"
        elif [[ "$line" =~ ^[0-9]+$ ]]; then
          local t="$line"; [[ "$t" -eq 0 ]] && PARSED_TIMEOUT="" || PARSED_TIMEOUT="$t"
        fi ;;
      *) ;;
    esac
  done <"$cfg"
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
write_spec() {
  local spec_dir="$1" ro="$2" rw="$3" hide="$4" net="$5" timeout="$6" tail="$7"
  mkdir -p "$spec_dir"
  cp "$ro"   "${spec_dir}/ro.paths"   2>/dev/null || : > "${spec_dir}/ro.paths"
  cp "$rw"   "${spec_dir}/rw.paths"   2>/dev/null || : > "${spec_dir}/rw.paths"
  cp "$hide" "${spec_dir}/hide.paths" 2>/dev/null || : > "${spec_dir}/hide.paths"
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
