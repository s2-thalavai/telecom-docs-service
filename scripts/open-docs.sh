#!/usr/bin/env bash
# Build the documentation (docs tests + Asciidoctor) and open it in a browser.
set -euo pipefail
cd "$(dirname "$0")/.."
${MVN:-mvn} -B -q prepare-package
FILE="$(pwd)/target/generated-docs/index.html"
echo "Docs: $FILE"
if command -v xdg-open >/dev/null; then xdg-open "$FILE"; elif command -v open >/dev/null; then open "$FILE"; fi
