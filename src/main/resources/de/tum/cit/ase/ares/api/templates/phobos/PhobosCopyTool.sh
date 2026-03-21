#!/bin/bash
# Script to copy required files to /var/tmp/opt/core/
set -e  # Exit immediately if a command exits with a non-zero status
set -u  # Treat unset variables as an error
TARGET_DIR=“/var/tmp/opt/core”
# Copy SpecificExercise.cfg to target directory
cp -v SpecificExercise.cfg "$TARGET_DIR/"