#!/usr/bin/env bash
# shellcheck shell=bash
set -euo pipefail
HERE="$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=phobos-common.sh
source "${HERE}/phobos-common.sh"

usage() {
  cat <<USAGE
Usage:
  phobos.sh [layer options] [--config <file>]... -- <build_command> [args...]
  phobos.sh [layer options] [--config <file>]... <build_command> [args...]

Layer options (all enabled by default):
  --no-timeout       Disable timeout wrapper (phobos-timeout.sh).
  --no-network       Disable network sandbox (libnetblocker / phobos-network.sh).
  --no-filesystem    Disable filesystem sandbox (bwrap / phobos-filesystem.sh).

Notes:
- Base config: any "${HERE}/Base*.cfg" (INI-like) is applied first (sorted).
- Exercise configs: only files passed via --config/-c are applied in order;
  per-path FS merge, NET union, TIMEOUT last-wins.
- Tail config: "${HERE}/TailPhobos.cfg" (flags only) is applied last.
- If no Base*.cfg is present, Phobos runs the command raw (no sandbox).
USAGE
  exit 2
}

[[ $# -lt 1 ]] && usage

# Layer toggles (default: all enabled)
enable_timeout=1
enable_network=1
enable_filesystem=1

cfgs=()
cmd=()
while (( "$#" )); do
  case "$1" in
    --config|-c)
      shift
      [[ $# -gt 0 ]] || usage
      [[ -f "$1" ]] || { echo "Config not found: $1" >&2; exit ${PHB_EPOLICY}; }
      cfgs+=("$1"); shift;;
    --no-timeout)
      enable_timeout=0; shift;;
    --no-network)
      enable_network=0; shift;;
    --no-filesystem|--no-fs)
      enable_filesystem=0; shift;;
    --)
      shift
      while (( "$#" )); do cmd+=("$1"); shift; done
      break;;
    *)
      cmd+=("$1"); shift;;
  esac
done
[[ ${#cmd[@]} -eq 0 ]] && usage

# Export layer selection for inner scripts
export PHB_ENABLE_TIMEOUT="$enable_timeout"
export PHB_ENABLE_NETWORK="$enable_network"
export PHB_ENABLE_FILESYSTEM="$enable_filesystem"

mapfile -t base_cfgs < <(ls -1 "${HERE}"/Base*.cfg 2>/dev/null | sort || true)
if [[ ${#base_cfgs[@]} -eq 0 ]]; then
  _log "No Base*.cfg found; running command without sandbox."
  exec "${cmd[@]}"
fi

: "${TAIL_FLAGS_FILE:=${HERE}/TailPhobos.cfg}"
INI_TMP_DIRS=""

base_ro="$(mktemp)"; base_rw="$(mktemp)"; base_hide="$(mktemp)"; base_net="$(mktemp)"
: >"$base_ro"; : >"$base_rw"; : >"$base_hide"; : >"$base_net"
timeout_eff=""

# Build base policy (FS union, NET union, TIMEOUT last-wins)
for b in "${base_cfgs[@]}"; do
  parse_cfg_policy "$b"
  # FS: union only (no least-privilege checks while building base)
  fs_union_files "$base_ro" "$base_rw" "$base_hide" \
                 "$PARSED_RO_FILE" "$PARSED_RW_FILE" "$PARSED_HIDE_FILE"
  # NET: union
  tmpnet="$(mktemp)"; net_union "$tmpnet" "$base_net" "$PARSED_NET_FILE"; mv "$tmpnet" "$base_net"
  # TIMEOUT: last base wins
  [[ -n "${PARSED_TIMEOUT:-}" || "${PARSED_TIMEOUT:-__unset__}" == "" ]] && timeout_eff="${PARSED_TIMEOUT:-}"
done

# Effective policy (start from base, then apply exercise overrides)
eff_ro="$(mktemp)"; eff_rw="$(mktemp)"; eff_hide="$(mktemp)"; eff_net="$(mktemp)"
cp "$base_ro" "$eff_ro"; cp "$base_rw" "$eff_rw"; cp "$base_hide" "$eff_hide"; cp "$base_net" "$eff_net"

for c in "${cfgs[@]}"; do
  parse_cfg_policy "$c"
  merge_fs_per_path "$base_ro" "$base_rw" "$base_hide" eff_ro eff_rw eff_hide \
                    "$PARSED_RO_FILE" "$PARSED_RW_FILE" "$PARSED_HIDE_FILE"
  tmpnet="$(mktemp)"; net_union "$tmpnet" "$eff_net" "$PARSED_NET_FILE"; mv "$tmpnet" "$eff_net"
  [[ -n "${PARSED_TIMEOUT:-}" || "${PARSED_TIMEOUT:-__unset__}" == "" ]] && timeout_eff="${PARSED_TIMEOUT:-}"
done

SPEC_DIR="$(mktemp -d -t phobos-spec.XXXXXX)"
trap 'rm -rf "$SPEC_DIR" $INI_TMP_DIRS "$base_ro" "$base_rw" "$base_hide" "$base_net" "$eff_ro" "$eff_rw" "$eff_hide" "$eff_net"' EXIT
write_spec "$SPEC_DIR" "$eff_ro" "$eff_rw" "$eff_hide" "$eff_net" "$timeout_eff" "${TAIL_FLAGS_FILE:-}"

# Always enter through the first layer; inner scripts decide whether to apply themselves
exec "${HERE}/phobos-timeout.sh" "$SPEC_DIR" -- "${cmd[@]}"
