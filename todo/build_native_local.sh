#!/bin/bash
# Install Graalvm 
# brew install --cask graalvm-jdk
# /Library/Java/JavaVirtualMachines/graalvm-25.jdk
START_BUILD_APP=$(date +%s)
# For OSX
JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.jdk/Contents/Home
PATH=$JAVA_HOME/bin:$PATH
mvn clean package -DskipTests -Pnative
END_BUILD_APP=$(date +%s)
BUILD_APP_TIME=$(expr ${END_BUILD_APP} - ${START_BUILD_APP})
BUILD_APP_TIME=$(expr ${BUILD_APP_TIME} / 60 )
echo "Elasped time to build container ${BUILD_APP_TIME} minutes"
file target/*runner*
