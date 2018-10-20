#!/bin/sh

if [ "$CIRCLE_BRANCH" = "master" ] && [ "$CIRCLE_USERNAME" = "slipset" ]
then
    lein deploy clojars
else
    echo "Skipped command `lein deploy clojars`";
    exit 0;
fi
