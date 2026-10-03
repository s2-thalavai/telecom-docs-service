#!/usr/bin/env bash
# Run locally with H2 and sample data. Builds the docs first so /docs/index.html is available.
set -euo pipefail
cd "$(dirname "$0")/.."
MVN="${MVN:-mvn}"
command -v "$MVN" >/dev/null 2>&1 || { echo "Maven not found. Install Maven 3.9+ or use 'make up' (Docker)."; exit 1; }
if [[ ! -f target/classes/static/docs/index.html ]]; then
  echo "Building documentation (runs the documentation tests)..."
  "$MVN" -B -q prepare-package
fi
echo "Starting on http://localhost:${SERVER_PORT:-8080}  (docs: /docs/index.html)"
exec "$MVN" -B spring-boot:run
