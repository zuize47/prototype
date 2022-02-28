#!/bin/sh

echo "*****Running unit tests******"

git stash -q --keep-index

./gradlew test

status=$?

git stash pop -q

echo "*****Done with unit tests******"

exit $status