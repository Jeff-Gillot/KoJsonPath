#!/bin/sh
./gradlew clean
./gradlew publishMavenPublicationToLocalRepository
rm -f ./bundle.zip
(cd build/staging-deploy && zip -r ../../bundle.zip .)
