#!/bin/bash
set -eux

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $SCRIPT_DIR/..

OLD_VERSION="$1"
NEW_VERSION="$2"

# Add the new version
sed -i '' -e "s/val version = \"\(.*\)\" \/\/ updated by craft/val version = \"$NEW_VERSION\" \/\/ updated by craft/g" buildSrc/src/main/java/Config.kt
# Increment the buildVersionCode
perl -pi -e 's{val buildVersionCode = (\d+) // updated by craft}{$n=$1+1; "val buildVersionCode = $n // updated by craft"}e' buildSrc/src/main/java/Config.kt
