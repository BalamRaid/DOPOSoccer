#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"

JUNIT="lib/junit-platform-console-standalone-1.10.2.jar"
rm -rf bin
mkdir -p bin
trap 'rm -f sources.tmp' EXIT

find src -name '*.java' > sources.tmp
javac -encoding UTF-8 -cp "$JUNIT" -d bin @sources.tmp
java -jar "$JUNIT" execute --class-path bin --select-package doposoccer.test --details=summary
