#!/usr/bin/env bash
# shellcheck shell=bash
set -euo pipefail
HERE="$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=phobos-common.sh
source "${HERE}/phobos-common.sh"

[[ $# -ge 3 && "$2" == "--" ]] || { echo "Usage: phobos-network.sh <SPEC_DIR> -- <cmd...>"; exit 2; }
SPEC_DIR="$1"; shift 2
CMD=("$@")

enable_network="${PHB_ENABLE_NETWORK:-1}"

# If network layer is disabled, just pass through to filesystem layer.
if [[ "$enable_network" != "1" ]]; then
  exec "${HERE}/phobos-filesystem.sh" "${SPEC_DIR}" -- "${CMD[@]}"
fi

RULES="${SPEC_DIR}/net.rules"
: "${NETBLOCKER_SO:=${HERE}/libnetblocker.so}"

# Always define env vars for clarity
export LD_PRELOAD="${LD_PRELOAD:-}"
export NETBLOCKER_CONF="${NETBLOCKER_CONF:-}"

# If libnetblocker.so exists, always inject it via LD_PRELOAD,
# regardless of whether net.rules is empty or not.
if [[ -f "${NETBLOCKER_SO}" ]]; then
  # Remember the path so the filesystem layer can bind it into the sandbox
  export PHB_NETBLOCKER_SO="${NETBLOCKER_SO}"

  case ":${LD_PRELOAD}:" in
    *":${NETBLOCKER_SO}:"*) ;; # already there
    *) export LD_PRELOAD="${NETBLOCKER_SO}${LD_PRELOAD:+:${LD_PRELOAD}}";;
  esac

  # Use the spec's net.rules file as config; ensure it exists.
  if [[ -f "$RULES" ]]; then
    export NETBLOCKER_CONF="$RULES"
  else
    : > "$RULES"
    export NETBLOCKER_CONF="$RULES"
  fi
fi

exec "${HERE}/phobos-filesystem.sh" "${SPEC_DIR}" -- "${CMD[@]}"
