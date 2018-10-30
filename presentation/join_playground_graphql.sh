#!/usr/bin/env bash

#!/usr/bin/env bash

info() { printf "\\033[38;5;040mℹ\\033[0m %s\\n" "$1"; }
error() { printf "\\033[38;5;124m✗\\033[0m %s\\n" "$1"; }
debug() { printf "\\033[38;5;033m✓\\033[0m %s\\n" "$1"; }
pushd () { command pushd "$@" > /dev/null;  }
popd () { command popd "$@" > /dev/null; }

function control_c() {
	exit 1
}
trap control_c INT


join::usage() {
  if [ -n "$1" ]; then
    echo ""
    error "👉 $1";
    echo ""
  fi
  echo "Usage: $0 --initiative-id <id>"
  echo "  -i, --initiative-id      An initiative as created using ./import_playground_graphql.sh"
  echo ""
  exit 1
}

# parse params
while [[ "$#" -gt 0 ]]; do case $1 in
  -i|--initiative-id) INITIATIVE_ID="$2"; shift;shift;;

  *) join::usage "Unknown parameter passed: $1"; shift; shift;;
esac; done

# verify params
if [[ -z "${INITIATIVE_ID}" ]]; then join::usage "Initiative ID is not set"; fi;


timestamp() {
  date +"%T"
}

# http://localhost:4002/?query=mutation%20JoinInitiative(%24input%3A%20JoinInitiativeInput!)%20%7B%0A%20%20joinInitiative(input%3A%20%24input)%20%7B%0A%20%20%20%20id%0A%20%20%7D%0A%7D&operationName=JoinInitiative&variables=%7B%0A%20%20%22input%22%3A%20%7B%0A%20%20%20%20%22initiativeId%22%3A%20%22initiative-101%22%2C%0A%20%20%20%20%22citizenId%22%3A%20%22citizen-2%22%0A%20%20%7D%0A%7D
join_initiative() {
    local initiativeId=$1
    local citizenId="citizen-$(timestamp)"
    curl -o /dev/null -s -w "%{http_code}\n" \
        -H "Content-Type: application/json" \
        --data "{\"query\":\"mutation JoinInitiative(\$input: JoinInitiativeInput!) {\n  joinInitiative(input: \$input) {\n    id\n  }\n}\",\"variables\":{\"input\":{\"initiativeId\":\"${initiativeId}\",\"citizenId\":\"${citizenId}\"}},\"operationName\":\"JoinInitiative\"}" \
        localhost:18086/graphql
}
#while sleep 2; do debug "Joining initiative ${INITIATIVE_ID}: HTTP $(join_initiative ${INITIATIVE_ID})"; done
debug "Joining initiative ${INITIATIVE_ID}: HTTP $(join_initiative ${INITIATIVE_ID})"
