#!/bin/sh

if [ ! -z "$CIRCLE_PULL_REQUEST" ]
then
    echo "Skipped clojars push because this is a PR"
    exit 0;
fi

if [ "$CIRCLE_BRANCH" = "master" ] && [ "$CIRCLE_USERNAME" = "slipset" ]
then
    lein deploy clojars
else
    echo "Skipped clojars push because not master branch";
    exit 0;
fi
