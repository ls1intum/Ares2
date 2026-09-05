#!/usr/bin/env bash
# Produces and verifies the vendored network sandbox library. No Maven build compiles C,
# so `libnetblocker.so` and the `netblocker.c` beside it can drift apart unnoticed.
# No argument rebuilds both the library and its provenance; `--check` verifies a checkout;
# `--check-jar` verifies the same three files inside a packaged jar.
set -euo pipefail

HERE="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPOSITORY="$(cd -- "${HERE}/../.." && pwd)"
TEMPLATES="${REPOSITORY}/src/main/resources/de/tum/cit/ase/ares/api/templates/phobos"
SOURCE_NAME="netblocker.c"
ARTEFACT_NAME="libnetblocker.so"
MANIFEST_NAME="netblocker.provenance"
MANIFEST="${TEMPLATES}/${MANIFEST_NAME}"

# Where Maven puts the three files inside the jar it builds.
JAR_DIRECTORY="de/tum/cit/ase/ares/api/templates/phobos"

# The only architecture this library is built and tested for. Anything else is refused
# rather than shipped unverified.
SUPPORTED_ARCHITECTURE="x86_64"

# The newest glibc the library may demand. A newer toolchain can resolve the same calls to
# newer-versioned symbols, silently stopping the library loading on runtimes it serves today.
MAXIMUM_GLIBC="2.34"

# Output must be reproducible: `--build-id=none` drops an identifier that varies with the
# build path, and compiling by basename from inside the source directory, without `-g`,
# keeps absolute paths out of the result.
# shellcheck disable=SC2054  # the comma in -Wl,--build-id=none belongs to the flag.
COMPILE_FLAGS=(-fPIC -shared -Wl,--build-id=none)

# A configuration path opened by libraries predating NETBLOCKER_CONF. Its presence means the
# library was built from an older source than the one beside it.
ABANDONED_PATH="/var/tmp/opt/core/allowedList.cfg"

# Rule sets with a known verdict, each written as `rules|port`. Only the file NETBLOCKER_CONF
# names differs between them, so a library reading any other file answers them all alike and
# cannot produce this sequence.
DECIDING_CASES=("203.0.113.1 *|9" "127.0.0.1 *|9" "|9" "127.0.0.1 9|9")
DECIDING_ANSWERS="DENIED ALLOWED DENIED ALLOWED"

# Rule sets covering the rest of the parser and matcher. The shipped library and a rebuild
# must answer these alike; which answer is correct is deliberately not asserted, because this
# gate checks that two libraries agree rather than that the matching logic is right.
COMPARED_CASES=(
  "10.0.0.0/8 *|9"
  "127.0.0.0/8 *|9"
  "::ffff:127.0.0.1/128 *|9"
  "127.0.0.1/33 *|9"
  "127.0.0.1/abc *|9"
  "* *|9"
  "*|9"
  "127.0.0.1 80|9"
  "127.0.0.1 80|80"
  "*.example.com *|9"
  "# only a comment|9"
  "   |9"
)

# Reports why the run stopped and leaves with the given status.
fail() {
  echo "build-netblocker.sh: $1" >&2
  exit "${2:-1}"
}

# Refuses a host this library is neither built nor tested for, or one lacking the tools.
require_supported_host() {
  local architecture
  architecture="$(uname -m)"
  [[ "${architecture}" == "${SUPPORTED_ARCHITECTURE}" ]] ||
    fail "supported on ${SUPPORTED_ARCHITECTURE} only, but this host is ${architecture}"
  command -v gcc >/dev/null 2>&1 || fail "gcc is required but is not on PATH"
  command -v readelf >/dev/null 2>&1 || fail "readelf is required but is not on PATH"
}

# Compiles into a directory of its own, so that a build never sees another build's output.
build_into() {
  local destination="$1"
  cp -- "${TEMPLATES}/${SOURCE_NAME}" "${destination}/${SOURCE_NAME}"
  ( cd -- "${destination}" && gcc "${COMPILE_FLAGS[@]}" -o "${ARTEFACT_NAME}" "${SOURCE_NAME}" )
}

# Prints the SHA-256 of a file, without the name sha256sum writes after it.
digest_of() {
  sha256sum -- "$1" | cut -d ' ' -f 1
}

# Prints the newest glibc release a library demands of whatever loads it.
required_glibc() {
  readelf -V -- "$1" | grep -oE 'GLIBC_[0-9]+(\.[0-9]+)*' | sed 's/GLIBC_//' | sort -V | tail -n 1
}

# Refuses a library asking for a newer glibc than the ceiling above; it would pass every
# other check here and still fail to load where it is needed.
check_glibc_ceiling() {
  local required newest
  required="$(required_glibc "$1")"
  newest="$(printf '%s\n%s\n' "${required}" "${MAXIMUM_GLIBC}" | sort -V | tail -n 1)"
  [[ "${newest}" == "${MAXIMUM_GLIBC}" ]] ||
    fail "the library needs glibc ${required}, past the ${MAXIMUM_GLIBC} ceiling; build it on the recorded toolchain" 8
  echo "glibc: the library asks for no more than ${MAXIMUM_GLIBC}"
}

# Reads one value out of a manifest, the tracked one unless another is named. Carriage
# returns are stripped because .gitattributes checks these files out with CRLF endings.
manifest_value() {
  tr -d '\r' < "${2:-${MANIFEST}}" | sed -n "s/^$1=//p"
}

# Records what the artefact beside it was built from. Every value is computed before the
# manifest is opened, so a failing command stops the run instead of writing a blank entry.
write_manifest() {
  local source_digest artefact_digest compiler glibc
  source_digest="$(digest_of "${TEMPLATES}/${SOURCE_NAME}")"
  artefact_digest="$(digest_of "$1")"
  compiler="$(gcc --version | head -n 1)"
  glibc="$(required_glibc "$1")"
  {
    echo "# Provenance of ${ARTEFACT_NAME}, written by tools/netblocker/build-netblocker.sh."
    echo "# Rerun that script after editing ${SOURCE_NAME}, to renew both the library and this file."
    echo "source=${SOURCE_NAME}"
    echo "source-sha256=${source_digest}"
    echo "artifact=${ARTEFACT_NAME}"
    echo "artifact-sha256=${artefact_digest}"
    echo "architecture=${SUPPORTED_ARCHITECTURE}"
    echo "glibc-required=${glibc}"
    echo "compile-flags=${COMPILE_FLAGS[*]}"
    echo "compiler=${compiler}"
  } > "${MANIFEST}"
}

# The manifest records what the shipped library was made from. A source edited without
# rerunning this script leaves the recorded digest behind, which is the drift to catch.
check_manifest() {
  local recorded actual
  [[ -f "${MANIFEST}" ]] ||
    fail "no provenance was recorded for ${ARTEFACT_NAME}; rerun this script without --check" 4
  recorded="$(manifest_value source-sha256)"
  actual="$(digest_of "${TEMPLATES}/${SOURCE_NAME}")"
  [[ "${recorded}" == "${actual}" ]] ||
    fail "${SOURCE_NAME} changed since ${ARTEFACT_NAME} was built; rerun without --check" 4
  recorded="$(manifest_value artifact-sha256)"
  actual="$(digest_of "${TEMPLATES}/${ARTEFACT_NAME}")"
  [[ "${recorded}" == "${actual}" ]] ||
    fail "${ARTEFACT_NAME} does not match its recorded digest; rerun without --check" 4
  echo "manifest: the recorded digests match the checked-in source and library"
}

# Two builds of one source on one toolchain must agree byte for byte, or the flags above are
# not deterministic and no later comparison means anything.
check_reproducible() {
  mkdir -p -- "$1/first" "$1/second"
  build_into "$1/first"
  build_into "$1/second"
  cmp -s "$1/first/${ARTEFACT_NAME}" "$1/second/${ARTEFACT_NAME}" ||
    fail "two builds of ${SOURCE_NAME} differ, so the build is not reproducible" 3
  echo "reproducible: two clean builds agree ($(digest_of "$1/first/${ARTEFACT_NAME}"))"
}

# Reports whether this build matches the shipped library byte for byte. A different compiler
# release or flag set arranges the same code differently, so a difference is stated without
# inferring its cause, and the behavioural runs below carry the gate.
report_byte_equality() {
  if cmp -s "${TEMPLATES}/${ARTEFACT_NAME}" "$1/first/${ARTEFACT_NAME}"; then
    echo "identical: the current build is byte-identical to the shipped library"
  else
    echo "different: the current build is not byte-identical to the shipped library"
    echo "  compiler recorded for the shipped library: $(manifest_value compiler)"
    echo "  compiler used for the current build:       $(gcc --version | head -n 1)"
  fi
}

decision_of() {
  local workspace="$1" library="$2" rules="$3" port="$4"
  printf '%s\n' "${rules}" > "${workspace}/selected.rules"
  LD_PRELOAD="${workspace}/${library}.so" NETBLOCKER_CONF="${workspace}/selected.rules" \
    "${workspace}/probe" 127.0.0.1 "${port}"
}

# Prints one library's verdicts in the order the rule sets were given, so two libraries can
# be compared position by position.
decisions_of() {
  local workspace="$1" library="$2" answers="" entry
  shift 2
  for entry in "$@"; do
    answers="${answers}$(decision_of "${workspace}" "${library}" "${entry%|*}" "${entry##*|}") "
  done
  echo "${answers% }"
}

# Asks both libraries the same questions and requires the same answers. A rebuilt library
# and the shipped one may differ byte for byte, but they cannot differ in what they permit.
check_behavioural_equivalence() {
  build_probe "$1"
  cp -- "${TEMPLATES}/${ARTEFACT_NAME}" "$1/shipped.so"
  cp -- "$1/first/${ARTEFACT_NAME}" "$1/rebuilt.so"
  local shipped rebuilt
  shipped="$(decisions_of "$1" shipped "${DECIDING_CASES[@]}")"
  rebuilt="$(decisions_of "$1" rebuilt "${DECIDING_CASES[@]}")"
  echo "configuration source, shipped: ${shipped}"
  echo "configuration source, rebuilt: ${rebuilt}"
  [[ "${rebuilt}" == "${DECIDING_ANSWERS}" ]] ||
    fail "a rebuild of ${SOURCE_NAME} does not follow NETBLOCKER_CONF" 6
  [[ "${shipped}" == "${rebuilt}" ]] ||
    fail "the shipped library does not follow NETBLOCKER_CONF as ${SOURCE_NAME} does" 6
  shipped="$(decisions_of "$1" shipped "${COMPARED_CASES[@]}")"
  rebuilt="$(decisions_of "$1" rebuilt "${COMPARED_CASES[@]}")"
  echo "matching rules,       shipped: ${shipped}"
  echo "matching rules,       rebuilt: ${rebuilt}"
  [[ "${shipped}" == "${rebuilt}" ]] ||
    fail "the shipped library and a rebuild of ${SOURCE_NAME} match rules differently" 6
  echo "behaviour: both decide alike on ${#DECIDING_CASES[@]} + ${#COMPARED_CASES[@]} rule sets"
}

# Supplements the runs above, which decide the matter: a path absent from a file still says
# nothing about which file is read. Only grep status 1 counts as absence, so an unreadable
# library is an error rather than a clean result.
check_abandoned_path_absent() {
  local status=0
  grep -q -- "${ABANDONED_PATH}" "${TEMPLATES}/${ARTEFACT_NAME}" || status=$?
  [[ "${status}" -eq 1 ]] ||
    fail "the shipped ${ARTEFACT_NAME} carries ${ABANDONED_PATH}, or could not be read (grep ${status})" 7
  echo "contents: the shipped library no longer carries ${ABANDONED_PATH}"
}

# Names the tool that reads a jar. Requiring a JDK costs nothing, since a jar only exists
# where one built it.
jar_tool() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/jar" ]]; then
    echo "${JAVA_HOME}/bin/jar"
  elif command -v jar >/dev/null 2>&1; then
    command -v jar
  else
    fail "jar is required to read a packaged artefact but is not on PATH and JAVA_HOME names none" 9
  fi
}

# Counts one entry in a jar. Duplicates matter: a reader takes whichever copy it meets
# first, so two copies of a rule file are two possible answers.
jar_entry_count() {
  "$2" tf "$1" | grep -c -Fx -- "$3" || true
}

# Checks the three files Maven packages beside each other. It reads the jar and the checkout
# and writes to neither, so a released artefact can be checked as well as a fresh build.
check_packaged_jar() {
  local jar="$1" tool extracted name count
  [[ -f "${jar}" ]] || fail "no jar to check at ${jar}" 9
  jar="$(cd -- "$(dirname -- "${jar}")" && pwd)/$(basename -- "${jar}")"
  tool="$(jar_tool)"
  extracted="${WORKSPACE}/jar"
  mkdir -p -- "${extracted}"

  for name in "${ARTEFACT_NAME}" "${SOURCE_NAME}" "${MANIFEST_NAME}"; do
    count="$(jar_entry_count "${jar}" "${tool}" "${JAR_DIRECTORY}/${name}")"
    [[ "${count}" -ne 0 ]] || fail "the jar has no ${JAR_DIRECTORY}/${name}" 9
    [[ "${count}" -eq 1 ]] || fail "the jar has ${count} copies of ${JAR_DIRECTORY}/${name}" 9
  done
  echo "jar entries: all three present exactly once in ${jar}"

  ( cd -- "${extracted}" && "${tool}" xf "${jar}" "${JAR_DIRECTORY}" )
  for name in "${ARTEFACT_NAME}" "${SOURCE_NAME}" "${MANIFEST_NAME}"; do
    cmp -s "${extracted}/${JAR_DIRECTORY}/${name}" "${TEMPLATES}/${name}" ||
      fail "the packaged ${name} is not the ${name} in this checkout" 9
  done
  echo "jar bytes: every entry matches this checkout, so Maven altered none of them"

  local packaged_manifest recorded actual
  packaged_manifest="${extracted}/${JAR_DIRECTORY}/${MANIFEST_NAME}"
  recorded="$(manifest_value source-sha256 "${packaged_manifest}")"
  actual="$(digest_of "${extracted}/${JAR_DIRECTORY}/${SOURCE_NAME}")"
  [[ "${recorded}" == "${actual}" ]] ||
    fail "the packaged manifest records source digest ${recorded:-<none>}, but its ${SOURCE_NAME} hashes to ${actual}" 9
  recorded="$(manifest_value artifact-sha256 "${packaged_manifest}")"
  actual="$(digest_of "${extracted}/${JAR_DIRECTORY}/${ARTEFACT_NAME}")"
  [[ "${recorded}" == "${actual}" ]] ||
    fail "the packaged manifest records artifact digest ${recorded:-<none>}, but its ${ARTEFACT_NAME} hashes to ${actual}" 9
  echo "jar provenance: both recorded digests recompute from the jar's own entries"
}

# Writes and compiles the program the behavioural runs use to observe one decision.
build_probe() {
  cat > "$1/probe.c" <<'PROBE_EOF'
#define _GNU_SOURCE
#include <arpa/inet.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

/* Reports whether an interposed connect() refused the address or let it through. */
int main(int argc, char **argv)
{
	struct sockaddr_in address;
	memset(&address, 0, sizeof address);
	address.sin_family = AF_INET;
	address.sin_port = htons((unsigned short) (argc > 2 ? atoi(argv[2]) : 9));
	inet_pton(AF_INET, argc > 1 ? argv[1] : "127.0.0.1", &address.sin_addr);
	int descriptor = socket(AF_INET, SOCK_STREAM, 0);
	errno = 0;
	int outcome = connect(descriptor, (struct sockaddr *) &address, sizeof address);
	int failure = errno;
	close(descriptor);
	printf("%s", outcome != 0 && failure == EACCES ? "DENIED" : "ALLOWED");
	return 0;
}
PROBE_EOF
  ( cd -- "$1" && gcc -o probe probe.c )
}

WORKSPACE="$(mktemp -d)"
trap 'rm -rf -- "${WORKSPACE}"' EXIT

if [[ $# -eq 2 && "$1" == "--check-jar" ]]; then
  check_packaged_jar "$2"
  echo "build-netblocker.sh: the packaged ${ARTEFACT_NAME}, ${SOURCE_NAME} and ${MANIFEST_NAME}"
  echo "  are this checkout's, each packaged once, and agree with the packaged manifest"
elif [[ $# -eq 1 && "$1" == "--check" ]]; then
  require_supported_host
  check_manifest
  check_glibc_ceiling "${TEMPLATES}/${ARTEFACT_NAME}"
  check_reproducible "${WORKSPACE}"
  report_byte_equality "${WORKSPACE}"
  check_behavioural_equivalence "${WORKSPACE}"
  check_abandoned_path_absent
  echo "build-netblocker.sh: the shipped ${ARTEFACT_NAME} matches its recorded source and"
  echo "  artifact digests and the verified behavioural contract"
elif [[ $# -eq 0 ]]; then
  require_supported_host
  mkdir -p -- "${WORKSPACE}/first"
  build_into "${WORKSPACE}/first"
  check_glibc_ceiling "${WORKSPACE}/first/${ARTEFACT_NAME}"
  cp -- "${WORKSPACE}/first/${ARTEFACT_NAME}" "${TEMPLATES}/${ARTEFACT_NAME}"
  write_manifest "${TEMPLATES}/${ARTEFACT_NAME}"
  echo "built ${TEMPLATES}/${ARTEFACT_NAME} ($(digest_of "${TEMPLATES}/${ARTEFACT_NAME}"))"
  echo "wrote ${MANIFEST}"
else
  fail "usage: build-netblocker.sh [--check | --check-jar <jar>]" 2
fi
