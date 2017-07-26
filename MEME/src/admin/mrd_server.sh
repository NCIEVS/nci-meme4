#!/bin/sh
#
# Version Information
#   03/11/2005 2.2.0 - Use admin.pl -s kill
#   09/09/2004 2.1.0 - First version
#
release="2.0"
version="2.0"
version_date="03/11/2005"
version_auth="BAC"
usage="Usage: $0 <start|restart|stop|pid> <port> <mid service>"

# Set environment
. $ENV_HOME/bin/env.sh

# Set port and mid service
PORT=$2
MS=$3

grep=/bin/grep
ps=/bin/ps
cut=/bin/cut
awk=/bin/awk

pid=`ps -deaf | grep "java" | grep "meme" | grep -v grep | cut -c1-24 | awk '{print $2}'`
pid1=`ps -deaf | grep "memerun.pl" | grep -v grep | cut -c1-24 | awk '{print $2}'`

start_mrd() {
    case "x$MS" in
        x) MS=mrd-db;;
        *) ;;
    esac
	$MRD_HOME/bin/memerun.pl -port $PORT -prop $MRD_HOME/etc/mrd.prop -mid $MS -mrd $MS gov.nih.nlm.mrd.server.MRDApplicationServer 1>$MRD_HOME/log/`/bin/date +%Y%m%d`.$$.log 2>&1 &
}

stop_mrd() {
	$MRD_HOME/bin/admin.pl -s kill -host localhost -port $PORT
}



# Process the command line arguments...
# =====================================
case "$1" in
    start) start_mrd ;;
    restart) stop_mrd ; start_mrd ;;
    stop)  stop_mrd ;;
    pid)   echo "MRD server pids: " $pid $pid1; (ps -deaf | grep "java" | grep "meme" | grep -v grep); (ps -deaf | grep "memerun.pl" | grep -v grep) 
echo $pid_long; echo $pid1_long ;;
    *)     echo 1>&2 "$usage"
           exit 1 ;;
esac

exit 0

