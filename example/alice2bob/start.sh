#!/usr/bin/env bash

[ "$(echo "$1" | grep '\.jar$')" ] || {
    echo "ERROR: the first argument should be the path to the JAR file" ; exit 1 ;
}

java -Dconfig.file=settings.conf -jar $1  &

( cd outgoing && inkscape calculation.svg ) &
