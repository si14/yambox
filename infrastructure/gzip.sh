#!/bin/bash

for i in /opt/yambox/resources/public/css/*; do [ "${i:(-3)}" == ".gz" ] || gzip -c -9 "$i" > "$i.gz"; done
# for i in /opt/yambox/resources/public/js/*; do [ "${i:(-3)}" == ".gz" ] || gzip -c -9 "$i" > "$i.gz"; done
# for i in /opt/yambox/resources/public/static_js/*; do [ "${i:(-3)}" == ".gz" ] || gzip -c -9 "$i" > "$i.gz"; done
for i in /opt/yambox/resources/public/*; do [ "${i:(-3)}" == ".gz" ] || gzip -c -9 "$i" > "$i.gz"; done
