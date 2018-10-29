#!/usr/bin/env bash
curl -v \
    -H "Content-Type: application/json" \
    --data "{\"address\": {\"city\": \"Beerta\", \"number\": 12, \"street\": \"Bovenlandenstraat \", \"zipcode\": \"9686PP\"}, \"initiativeId\": \"${uuidgen}\", \"name\": \"Speulparadies\", \"status\": \"finished\", \"type\": \"smokefree\", \"website\": \"https://www.speeltuinbeerta.nl/kinderspeeltuin-speulparadies-oldambt-groningen-home-nl\"}" \
    localhost:18086/playgrounds
