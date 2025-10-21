#!/usr/bin/env bash
# shellcheck shell=bash
set -euo pipefail
HERE="$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=phobos-common.sh
source "${HERE}/phobos-common.sh"

[[ $# -ge 3 && "$2" == "--" ]] || { echo "Usage: phobos-network.sh <SPEC_DIR> -- <cmd...>"; exit 2; }
SPEC_DIR="$1"; shift 2
CMD=("$@")

RULES="${SPEC_DIR}/net.rules"
export LD_PRELOAD="${LD_PRELOAD:-}"
export NETBLOCKER_CONF="${NETBLOCKER_CONF:-}"

if [[ -s "$RULES" ]]; then
  : "${NETBLOCKER_SO:=${HERE}/libnetblocker.so}"
  if [[ -f "${NETBLOCKER_SO}" ]]; then
    export LD_PRELOAD="${NETBLOCKER_SO}${LD_PRELOAD:+:${LD_PRELOAD}}"
    NBCONF="$(mktemp -t phobos-net.XXXXXX)"; trap 'rm -f "$NBCONF"' EXIT
    cp "$RULES" "$NBCONF"; export NETBLOCKER_CONF="$NBCONF"
  fi
fi

exec "${HERE}/phobos-filesystem.sh" "${SPEC_DIR}" -- "${CMD[@]}"
