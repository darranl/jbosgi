#!/bin/sh

PROGNAME=`basename $0`
DIRNAME=`dirname $0`
RUNTIME_HOME="$1"
CMD="$2"
SERVER_NAME="$3"
BINDADDR="$4"

export RUNTIME_HOME
pidfile="$RUNTIME_HOME/bin/pid.txt"

if [ $CONTAINER = 'jboss700' ] || [ $CONTAINER = 'jboss70x' ]; then
    RUN_CMD=$RUNTIME_HOME/bin/standalone.sh
    export LAUNCH_JBOSS_IN_BACKGROUND="true"
    export JBOSS_PIDFILE=$pidfile
else
    RUN_CMD="$RUNTIME_HOME/bin/run.sh -c $SERVER_NAME -b $BINDADDR"
fi

#
# Helper to complain.
#
warn() {
   echo "$PROGNAME: $*"
}

#if [ ! -f "$RUNTIME_HOME/bin/run.sh" ]; then
#   warn "Cannot find: $RUNTIME_HOME/bin/run.sh"
#   exit 1
#fi

case "$CMD" in
start)
    # This version of run.sh obtains the pid of the JVM and saves it as jboss.pid
    # It relies on bash specific features
    /bin/bash $RUN_CMD &
    ;;
stop)
    if [ -f "$pidfile" ]; then
       pid=`cat "$pidfile"`
       echo "kill pid: $pid"
       kill $pid
       if [ "$?" -eq 0 ]; then
         # process exists, wait for it to die, and force if not
         sleep 20
         kill -9 $pid &> /dev/null
       fi
       rm "$pidfile"
    else
       warn "No pid found!"
    fi
    ;;
restart)
    $0 stop
    $0 start
    ;;
*)
    echo "usage: $0 jboss_home (start|stop|restart|help)"
esac
