#!/bin/sh
#
# A script that uses Maven to build the project and
# execute its test suite against a given target container 
#
# $Id$

OSGIDIR=$WORKSPACE/jboss-osgi
DISTRODIR=$OSGIDIR/distribution/installer/target
HUDSONDIR=$OSGIDIR/hudson
HUDSONBIN=$HUDSONDIR/hudson-home/bin

case "$CONTAINER" in
  'jboss501')
    SERVER_NAME=default
    JBOSS_BUILD=jboss-5.0.1.GA
    JBOSS_ZIP=$HUDSON_HOME/../jboss/$JBOSS_BUILD.zip
	RUNTIME_HOME=$WORKSPACE/$JBOSS_BUILD
	RUNTIME_LOG=$RUNTIME_HOME/server/$SERVER_NAME/log/server.log
    rm -rf $RUNTIME_HOME; unzip -q $JBOSS_ZIP -d $WORKSPACE  
    cp --backup $HUDSONBIN/run-with-pid.sh $RUNTIME_HOME/bin/run.sh
  ;;
  'jboss510')
    SERVER_NAME=default
    JBOSS_BUILD=jboss-5.1.0.GA
    JBOSS_ZIP=$HUDSON_HOME/../jboss/$JBOSS_BUILD.zip
	RUNTIME_HOME=$WORKSPACE/$JBOSS_BUILD
	RUNTIME_LOG=$RUNTIME_HOME/server/$SERVER_NAME/log/server.log
    rm -rf $RUNTIME_HOME; unzip -q $JBOSS_ZIP -d $WORKSPACE  
    cp --backup $HUDSONBIN/run-with-pid.sh $RUNTIME_HOME/bin/run.sh
  ;;
  'jboss600')
    SERVER_NAME=default
    JBOSS_BUILD=jboss-6.0.0.M1
    JBOSS_ZIP=$HUDSON_HOME/../jboss/$JBOSS_BUILD.zip
	RUNTIME_HOME=$WORKSPACE/$JBOSS_BUILD
	RUNTIME_LOG=$RUNTIME_HOME/server/$SERVER_NAME/log/server.log
    rm -rf $RUNTIME_HOME; unzip -q $JBOSS_ZIP -d $WORKSPACE  
    cp --backup $HUDSONBIN/run-with-pid.sh $RUNTIME_HOME/bin/run.sh
  ;;
  'jboss601')
    SERVER_NAME=default
    JBOSS_BUILD=jboss-6.0.0-SNAPSHOT
    JBOSS_ZIP=$HUDSON_HOME/../jboss/$JBOSS_BUILD.zip
	RUNTIME_HOME=$WORKSPACE/$JBOSS_BUILD
	RUNTIME_LOG=$RUNTIME_HOME/server/$SERVER_NAME/log/server.log
    rm -rf $RUNTIME_HOME; unzip -q $JBOSS_ZIP -d $WORKSPACE
    cp --backup $HUDSONBIN/run-with-pid.sh $RUNTIME_HOME/bin/run.sh
  ;;
  'runtime')
    SERVER_NAME=all
	RUNTIME_HOME=$DISTRODIR/auto-install-dest/runtime
	RUNTIME_LOG=$RUNTIME_HOME/server/$SERVER_NAME/log/server.log
  ;;
  *)
	echo "Unsupported container: $CONTAINER"
	exit 1
  ;;
esac

ENVIRONMENT="-Dframework=$FRAMEWORK -Dtarget.container=$CONTAINER -Djboss.home=$RUNTIME_HOME -Djboss.bind.address=$JBOSS_BINDADDR"

#
# Build distro
#
cd $OSGIDIR
MVN_CMD="mvn -U -Pdistro $ENVIRONMENT clean install"
echo $MVN_CMD; $MVN_CMD; MVN_STATUS=$?
if [ $MVN_STATUS -ne 0 ]; then
  echo maven exit status $MVN_STATUS
  exit 1
fi

#
# Deploy distro
#
cp $DISTRODIR/jboss-osgi-installer-*.jar $DISTRODIR/jboss-osgi-installer.jar
AUTO_INSTALL=$DISTRODIR/resources/auto-install-template.xml; cat $AUTO_INSTALL;
JAVA_CMD="java -jar $DISTRODIR/jboss-osgi-installer.jar $AUTO_INSTALL"
echo $JAVA_CMD; $JAVA_CMD 

#
# log dependency tree
#
MVN_CMD="mvn -o $ENVIRONMENT dependency:tree"
echo $MVN_CMD; $MVN_CMD | tee $WORKSPACE/dependency-tree.txt

#
# start jbossas/runtime
#
STARTUP_CMD="$HUDSONBIN/startup.sh $RUNTIME_HOME start $SERVER_NAME $JBOSS_BINDADDR"
echo $STARTUP_CMD; $STARTUP_CMD

#
# Was it successfully started?
#
$HUDSONBIN/http-spider.sh $JBOSS_BINDADDR:8090/jboss-osgi $WORKSPACE
if [ -e $WORKSPACE/spider.failed ]; then
  tail -n 200 $RUNTIME_LOG
  $HUDSONBIN/startup.sh $RUNTIME_HOME stop
  exit 1
fi

#
# execute tests
#
MVN_CMD="mvn -o -fae $ENVIRONMENT test"
echo $MVN_CMD; $MVN_CMD 2>&1 | tee $WORKSPACE/tests.log
cat $WORKSPACE/tests.log | egrep FIXME\|FAILED | sort -u | tee $WORKSPACE/fixme.txt
cat $WORKSPACE/fixme.txt | egrep "\[\S*]" > $WORKSPACE/errata-$CONTAINER.txt || :

#
# stop jbossas/runtime
#
$HUDSONBIN/startup.sh $RUNTIME_HOME stop
