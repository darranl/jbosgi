#!/bin/sh

JBOSS_HOME="$HOME/git/jboss-as-7.0.0.Final/build/target/jboss-as-7.0.0.Final"

cp api/target/jboss-osgi-example-jbossas-api-1.0.0-SNAPSHOT.jar $JBOSS_HOME/standalone/deployments
cp service/target/jboss-osgi-example-jbossas-service-1.0.0-SNAPSHOT.jar $JBOSS_HOME/standalone/deployments
cp ejb3/target/jboss-osgi-example-jbossas-ejb3-1.0.0-SNAPSHOT.jar $JBOSS_HOME/standalone/deployments
cp webapp/target/jboss-osgi-example-jbossas-webapp-1.0.0-SNAPSHOT.war $JBOSS_HOME/standalone/deployments

