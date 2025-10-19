#!/usr/bin/env bash
# shellcheck shell=bash
set -euo pipefail
HERE="$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=phobos-common.sh
source "${HERE}/phobos-common.sh"

[[ $# -ge 3 && "$2" == "--" ]] || { echo "Usage: phobos-timeout.sh <SPEC_DIR> -- <cmd...>"; exit 2; }
SPEC_DIR="$1"; shift 2
CMD=("$@")

if [[ -s "${SPEC_DIR}/timeout.sec" ]]; then
  PHB_TIMEOUT_SEC="$(<"${SPEC_DIR}/timeout.sec")"
  export PHB_TIMEOUT_SEC
else
  export PHB_TIMEOUT_SEC=""
fi

exec "${HERE}/phobos-network.sh" "${SPEC_DIR}" -- "${CMD[@]}"
