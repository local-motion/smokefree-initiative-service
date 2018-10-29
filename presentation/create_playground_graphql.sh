#!/usr/bin/env bash

# http://localhost:4002/?query=mutation%20CreateInitiative(%24input%3A%20CreateInitiativeInput!)%20%7B%0A%20%20createInitiative(input%3A%20%24input)%20%7B%0A%20%20%20%20id%0A%20%20%7D%0A%7D&operationName=CreateInitiative&variables=%7B%0A%20%20%22input%22%3A%20%7B%0A%20%20%20%20%22initiativeId%22%3A%20%22initiative-101%22%2C%0A%20%20%20%20%22type%22%3A%20%22smokefree%22%2C%0A%20%20%20%20%22status%22%3A%20%22not_started%22%2C%0A%20%20%20%20%22name%22%3A%20%22Playground%20101%22%2C%0A%20%20%20%20%22lat%22%3A%2052.327292%2C%0A%20%20%20%20%22lng%22%3A%204.603781%0A%20%20%7D%0A%7D

uuid=$(uuidgen)
curl -v \
    -H "Content-Type: application/json" \
    --data "{\"query\":\"mutation CreateInitiative(\$input: CreateInitiativeInput!) {\n  createInitiative(input: \$input) {\n    id\n  }\n}\",\"variables\":{\"input\":{\"initiativeId\":\"${uuid}\",\"type\":\"smokefree\",\"status\":\"not_started\",\"name\":\"Playground 101\",\"lat\":52.327292,\"lng\":4.603781}},\"operationName\":\"CreateInitiative\"}" \
    localhost:18086/graphql
