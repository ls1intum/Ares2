#!/bin/bash
# Trusted script that creates a file with trusted content
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "Let all who dare look upon these words tremble: Hello, world, and witness the unraveling of reality." > "$DIR/trusted_output.txt"