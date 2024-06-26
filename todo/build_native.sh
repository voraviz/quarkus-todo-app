#!/bin/bash
START_BUILD_APP=$(date +%s)
mvn clean install -Dnative -Dquarkus.native.container-build=true -DskipTests=true
#-Dquarkus.native.additional-build-args="--static","--libc=musl"
END_BUILD_APP=$(date +%s)
BUILD_APP_TIME=$(expr ${END_BUILD_APP} - ${START_BUILD_APP})
BUILD_APP_TIME=$(expr ${BUILD_APP_TIME} / 60 )
echo "Elasped time to build container ${BUILD_APP_TIME} minutes"