#!/bin/sh

# Verify JBOSS_HOME
if [ "x$JBOSS_HOME" = "x" ]; then
   echo "JBOSS_HOME not set"
   exit 1
fi

# Verify deployments dir
DEPLOYMENTS_DIR="$JBOSS_HOME/standalone/deployments"
if [ ! -d $DEPLOYMENTS_DIR ]; then
   echo "Deployments dir does not exist: $DEPLOYMENTS_DIR"
   exit 1
fi

# Find and deploy the target files
for TARGET in "api" "service"  "ejb3" "webapp"
do
	TARGET_FILE="`find $TARGET -type f -name 'jboss-osgi-example-jbossas-*' | grep -v sources | grep -v iml`"
	echo "Deploying $TARGET_FILE"
	cp $TARGET_FILE $DEPLOYMENTS_DIR
    sleep 1s
done 
