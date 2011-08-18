#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  JBossOSGi Bootstrap Script                                              ##
##                                                                          ##
### ====================================================================== ###

### 
DIRNAME=`dirname $0`
PROGNAME=`basename $0`
GREP="grep"

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

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
        
    Linux)
        linux=true
        ;;
esac

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then
    RUN_CONF="$DIRNAME/run.conf"
fi
if [ -r "$RUN_CONF" ]; then
    . "$RUN_CONF"
fi

# Force IPv4 on Linux systems since IPv6 doesn't work correctly with jdk5 and lower
if [ "$linux" = "true" ]; then
   JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Setup OSGI_HOME
if [ "x$OSGI_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    OSGI_HOME=`cd $DIRNAME/..; pwd`
fi
export OSGI_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
   JAVA="$JAVA_HOME/bin/java"
    else
   JAVA="java"
    fi
fi

# Setup the classpath
spijar="$OSGI_HOME/lib/jbosgi-spi.jar"
if [ ! -f "$spijar" ]; then
    die "Missing required file: $spijar"
fi
OSGI_BOOT_CLASSPATH="$OSGI_HOME/conf"
for file in $OSGI_HOME/lib/*; do
    OSGI_BOOT_CLASSPATH="$OSGI_BOOT_CLASSPATH:${file}"
done

if [ "x$OSGI_CLASSPATH" = "x" ]; then
    OSGI_CLASSPATH="$OSGI_BOOT_CLASSPATH"
else
    OSGI_CLASSPATH="$OSGI_CLASSPATH:$OSGI_BOOT_CLASSPATH"
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
        # MacOS does not support -server flag neither does it generally exist on Windows
        if [[ "$darwin" != "true" && "$cygwin" != "true" ]]; then
            JAVA_OPTS="-server $JAVA_OPTS"
        fi
    fi
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    OSGI_HOME=`cygpath --path --windows "$OSGI_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    OSGI_CLASSPATH=`cygpath --path --windows "$OSGI_CLASSPATH"`
fi

# Setup JBoss specific properties
JAVA_OPTS="-Dprogram.name=$PROGNAME -Dosgi.home=$OSGI_HOME $JAVA_OPTS"

# Display our environment
echo "========================================================================="
echo ""
echo "  JBossOSGi Bootstrap Environment"
echo ""
echo "  OSGI_HOME: $OSGI_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""

# Execute the JVM in the foreground
"$JAVA" $JAVA_OPTS \
  -classpath "$OSGI_CLASSPATH" \
  org.jboss.osgi.spi.framework.OSGiBootstrap "$@" &

OSGI_PID=$!
echo $OSGI_PID > $OSGI_HOME/bin/pid.txt

# Trap common signals and relay them to the jboss process
trap "kill -HUP  $OSGI_PID" HUP
trap "kill -TERM $OSGI_PID" INT
trap "kill -QUIT $OSGI_PID" QUIT
trap "kill -PIPE $OSGI_PID" PIPE
trap "kill -TERM $OSGI_PID" TERM

# Wait until the background process exits
WAIT_STATUS=128
while [ "$WAIT_STATUS" -ge 128 ]; do
  wait $OSGI_PID 2>/dev/null
  WAIT_STATUS=$?
  if [ "${WAIT_STATUS}" -gt 128 ]; then
    SIGNAL=`expr ${WAIT_STATUS} - 128`
    SIGNAL_NAME=`kill -l ${SIGNAL}`
    # echo "*** OSGi Runtime process (${OSGI_PID}) received ${SIGNAL_NAME} signal ***" >&2
  fi          
done
if [ "${WAIT_STATUS}" -lt 127 ]; then
  OSGI_STATUS=$WAIT_STATUS
else
  OSGI_STATUS=0
fi
