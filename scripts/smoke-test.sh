#!/usr/bin/env bash
# Checks a running instance against what the documentation promises.
# Usage: BASE_URL=http://localhost:8080 AGENT_PASSWORD=... ADMIN_PASSWORD=... ./scripts/smoke-test.sh
set -uo pipefail
BASE_URL="${BASE_URL:-http://localhost:8080}"
AGENT="agent:${AGENT_PASSWORD:-agent-pass}"
ADMIN="admin:${ADMIN_PASSWORD:-admin-pass}"
API="$BASE_URL/api/v1"
PASS=0; FAIL=0
green() { printf '\033[32m%s\033[0m\n' "$*"; }
red()   { printf '\033[31m%s\033[0m\n' "$*"; }
check() { if [[ "$3" == "$2" ]]; then green "PASS  $1 ($3)"; PASS=$((PASS+1)); else red "FAIL  $1 (got $3, want $2)"; FAIL=$((FAIL+1)); fi; }
code()  { curl -s -o /dev/null -w '%{http_code}' "$@"; }

echo "Waiting for $BASE_URL/actuator/health ..."
for _ in $(seq 1 60); do curl -fs "$BASE_URL/actuator/health" >/dev/null 2>&1 && break; sleep 2; done

echo "== Documentation =="
check "docs served publicly"         200 "$(code "$BASE_URL/docs/index.html")"
curl -s "$BASE_URL/docs/index.html" | grep -q 'id="customer-create"' && { green "PASS  docs contain customer-create"; PASS=$((PASS+1)); } \
  || { red "FAIL  docs missing customer-create"; FAIL=$((FAIL+1)); }

echo "== Documented behaviour =="
N=$(( (RANDOM * 32768 + RANDOM) % 100000000 ))
BODY="{\"name\":\"Smoke $N\",\"email\":\"smoke$N@example.com\",\"msisdn\":\"$(printf '9%09d' "$N")\",\"planCode\":\"UNL-599\"}"
check "plans public"                 200 "$(code "$API/plans")"
check "customers need auth (401)"    401 "$(code "$API/customers/1")"
LOC=$(curl -s -D - -o /dev/null -u "$AGENT" -H 'Content-Type: application/json' -d "$BODY" "$API/customers" \
      | tr -d '\r' | sed -n 's/^[Ll]ocation: //p')
[[ -n "$LOC" ]] && { green "PASS  create returns Location $LOC"; PASS=$((PASS+1)); } || { red "FAIL  create"; FAIL=$((FAIL+1)); }
ID="${LOC##*/}"
check "duplicate (409)"              409 "$(code -u "$AGENT" -H 'Content-Type: application/json' -d "$BODY" "$API/customers")"
check "validation (400)"             400 "$(code -u "$AGENT" -H 'Content-Type: application/json' -d '{"name":""}' "$API/customers")"
check "v2 read"                      200 "$(code -u "$AGENT" "$BASE_URL/api/v2/customers/$ID")"
check "terminate"                    200 "$(code -u "$AGENT" -X PATCH -H 'Content-Type: application/json' -d '{"status":"TERMINATED"}' "$API/customers/$ID/status")"
check "reactivate terminated (409)"  409 "$(code -u "$AGENT" -X PATCH -H 'Content-Type: application/json' -d '{"status":"ACTIVE"}' "$API/customers/$ID/status")"
check "agent delete (403)"           403 "$(code -u "$AGENT" -X DELETE "$API/customers/$ID")"
check "admin delete (204)"           204 "$(code -u "$ADMIN" -X DELETE "$API/customers/$ID")"
check "deleted (404)"                404 "$(code -u "$AGENT" "$API/customers/$ID")"

echo; echo "Passed: $PASS  Failed: $FAIL"
[[ $FAIL -eq 0 ]]
