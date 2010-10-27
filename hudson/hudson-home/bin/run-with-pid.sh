#!/bin/bash
### ====================================================================== ###
##                                                                          ##
##  JBoss Bootstrap Script                                                  ##
##                                                                          ##
### ====================================================================== ###

### 
DIRNAME=`dirname $0`
PROGNAME=`basename $0`
GREP="grep"

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

#
# Helper to complain.
#
warn() {
    echo "${PROGNAME}: $*"
}

#
# Helper to puke.
#
die() {
    warn $*
    exit 1
}

# Setup JBOSS_HOME
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi
export JBOSS_HOME

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then
    RUN_CONF="$JBOSS_HOME/bin/run.conf"
fi
if [ -r "$RUN_CONF" ]; then
    . "$RUN_CONF"
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

# Setup the classpath
runjar="$JBOSS_HOME/bin/run.jar"
if [ ! -f "$runjar" ]; then
    die "Missing required file: $runjar"
fi
JBOSS_BOOT_CLASSPATH="$runjar"

# Include the JDK javac compiler for JSP pages. The default is for a Sun JDK
# compatible distribution which JAVA_HOME points to
if [ "x$JAVAC_JAR" = "x" ]; then
    JAVAC_JAR="$JAVA_HOME/lib/tools.jar"
fi
if [ ! -f "$JAVAC_JAR" ]; then
   warn "Missing file: $JAVAC_JAR"
   warn "Unexpected results may occur.  Make sure JAVA_HOME points to a JDK and not a JRE."
fi

if [ "x$JBOSS_CLASSPATH" = "x" ]; then
    JBOSS_CLASSPATH="$JBOSS_BOOT_CLASSPATH:$JAVAC_JAR"
else
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_BOOT_CLASSPATH:$JAVAC_JAR"
fi

# If -server not set in JAVA_OPTS, set it, if supported
SERVER_SET=`echo $JAVA_OPTS | $GREP "\-server"`
if [ "x$SERVER_SET" = "x" ]; then

    # Check for SUN(tm) JVM w/ HotSpot support
    if [ "x$HAS_HOTSPOT" = "x" ]; then
	HAS_HOTSPOT=`"$JAVA" -version 2>&1 | $GREP -i HotSpot`
    fi

    # Enable -server if we have Hotspot, unless we can't
    if [ "x$HAS_HOTSPOT" != "x" ]; then
    JAVA_OPTS="-server $JAVA_OPTS"
    fi
fi

# Setup JBoss sepecific properties
JAVA_OPTS="-Dprogram.name=$PROGNAME $JAVA_OPTS"

# Force IPv4 on Linux systems since IPv6 doesn't work correctly with jdk5 and lower
JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

# This should be removed when JBoss configuration XML files can be validated JBAS-6744
JAVA_OPTS="$JAVA_OPTS -Dxb.builder.useUnorderedSequence=true"

# Setup the java endorsed dirs
JBOSS_ENDORSED_DIRS="$JBOSS_HOME/lib/endorsed"

# Display our environment
echo "========================================================================="
echo ""
echo "  JBoss Bootstrap Environment"
echo ""
echo "  JBOSS_HOME: $JBOSS_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  CLASSPATH: $JBOSS_CLASSPATH"
echo ""
echo "========================================================================="
echo ""
# enable monitor mode (job control needed)
set -m
STATUS=10
while [ $STATUS -eq 10 ]
do
# Execute the JVM
   "$JAVA" $JAVA_OPTS \
      -Djava.endorsed.dirs="$JBOSS_ENDORSED_DIRS" \
      -classpath "$JBOSS_CLASSPATH" \
      org.jboss.Main "$@" &> /dev/null &
      echo $! > $JBOSS_HOME/bin/pid.txt
      fg
   STATUS=$?
   # if it doesn't work, you may want to take a look at this:
   #    http://developer.java.sun.com/developer/bugParade/bugs/4465334.html
done
