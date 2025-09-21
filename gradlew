#!/bin/sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME to the directory containing this script
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

APP_HOME=`dirname "$PRG"`/..

APP_HOME=`cd "$APP_HOME" && pwd`

if [ -z "$JAVA_HOME" ]; then
  JAVA=java
else
  JAVA="$JAVA_HOME/bin/java"
fi

exec "$JAVA" \
  -Dorg.gradle.appname=gradle \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
