#!/usr/bin/env bash
curl -v \
    -H "Content-Type: application/json" \
    --data "{\"address\": {\"city\": \"Beerta\", \"number\": 12, \"street\": \"Bovenlandenstraat \", \"zipcode\": \"9686PP\"}, \"geoLocation\": {\"lat\": 53.2113225, \"lng\": 6.5671421}, \"initiativeId\": \"$(uuidgen)\", \"name\": \"t Speulparadies\", \"status\": \"finished\", \"type\": \"smokefree\", \"website\": \"https://www.speeltuinbeerta.nl/kinderspeeltuin-speulparadies-oldambt-groningen-home-nl\"}" \
    localhost:18086/playgrounds
#    --data "{\"address\": {\"city\": \"Beerta\", \"number\": 12, \"street\": \"Bovenlandenstraat \", \"zipcode\": \"9686PP\"}, \"initiativeId\": \"${uuidgen}\", \"name\": \"Speulparadies\", \"status\": \"finished\", \"type\": \"smokefree\", \"website\": \"https://www.speeltuinbeerta.nl/kinderspeeltuin-speulparadies-oldambt-groningen-home-nl\"}" \
