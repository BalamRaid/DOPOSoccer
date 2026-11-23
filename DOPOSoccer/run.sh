#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"

rm -rf bin
mkdir -p bin
trap 'rm -f sources.tmp' EXIT

find src/doposoccer/domain src/doposoccer/application \
     src/doposoccer/infrastructure src/doposoccer/presentation \
     -name '*.java' > sources.tmp
javac -encoding UTF-8 -d bin @sources.tmp
java -cp bin doposoccer.presentation.DOPOSoccerGUI
