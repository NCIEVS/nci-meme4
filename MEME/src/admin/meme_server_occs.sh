#!/bin/sh
#
# Version Information
#   03/11/2005 4.2.0 - use admin.pl -s kill
#   09/28/2004 4.1.0 - Ready for release
#   09/16/2004 4.0.5 - First version
#
release="4.0"
version="2.0"
version_date="03/11/2005"
version_auth="BAC"
usage="Usage: $0 <stop|start|restart|pid> <port> <mid service>"

# Set environment - ENV_HOME and ENV_FILE must be set
hostname=`hostname`;
ENV_HOME=/umls_prod/umls_apps/env
ENV_FILE=/umls_prod/umls_apps/env/etc/env.prop.midp.$hostname
export ENV_HOME
export ENV_FILE
. $ENV_HOME/bin/env.sh
PROPERTIES_FILE=$MEME_HOME/etc/meme.prop.$hostname
date=`/bin/date +%Y%m%d`;
time=`/bin/date +%H%M%S`;
LOG_FILE=/log/$date.$time.$hostname.log
export LOG_FILE
# Port and mid service settings.
PORT=$2
MS=$3

grep=/bin/grep
ps=/bin/ps
cut=/bin/cut
awk=/bin/awk

pid=`ps -deaf | grep "java" | grep "meme" | grep -v grep | cut -c1-24 | awk '{print $2}'`
pid1=`ps -deaf | grep "start.pl" | grep -v grep | cut -c1-24 | awk '{print $2}'`

start_meme() {
    case "x$MS" in
        x) MS=editing-db;;
        *) ;;
    esac
    $MEME_HOME/bin/start.pl -mid $MS -port $PORT -prop $PROPERTIES_FILE -log $LOG_FILE 1>>/dev/null 2>&1 &
}

stop_meme() {
    $MEME_HOME/bin/admin.pl -s kill -host localhost -port $PORT
}



# Process the command line arguments...
# =====================================
case "$1" in
    start) start_meme ;;
    restart) stop_meme ; start_meme ;;
    stop)  stop_meme ;;
    pid)   echo "MEME server pids: " $pid $pid1; (ps -deaf | grep "java" | grep "meme" | grep -v grep); (ps -deaf | grep "start.pl" | grep -v grep ) ;;
    *)     echo 1>&2 "$usage"
           exit 1 ;;
esac

exit 0

