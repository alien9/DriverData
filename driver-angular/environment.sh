#!/bin/bash

  #production: false,
  #api:'http://localhost:8000',
  #mapserver: 'http://localhost:5000',
  #label: 'Accidente',
  #center: [-23.5, -46.5], //[-16.5, -68.15]
  #zoom:12
PROTO=http
MAPSERVER=$(docker inspect  -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' windshaft-sp):5000
API=$(docker inspect  -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' driver-django-sp):8000
API=localhost:8000

sed -i -e "s/mapserver:.*/mapserver:\"${PROTO}:\/\/${MAPSERVER}\",/g" \
    -e "s/api:.*/api: \"${PROTO}:\/\/${API}\",/g" \
    -e "s/label:.*/label: \"Accidente\",/g" \
    -e "s/center:.*/center: [-16.5, -68.15],/g" \
    -e "s/zoom:.*/zoom: 12/g" \
    src/environments/environment.ts