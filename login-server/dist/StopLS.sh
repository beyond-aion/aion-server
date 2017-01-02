#!/bin/bash
#=====================================================================================
# Usage:        StopLS.sh [-f]
# Parameters:   -f
#                   forces termination (may lead to loss of data!)
# Description:  Performs a shutdown of LS if running.
#=====================================================================================

dir=$(dirname $0)
pidfile=${dir}/loginserver.pid
if [ -f $pidfile ] && ps -p `cat $pidfile` > /dev/null 2>&1; then
  pid=`cat $pidfile`
  if [ $# -gt 0 ] && [ $1 = "-f" ]; then
    kill -KILL $pid
    echo "LoginServer was killed."
  else
    kill $pid
    echo "LoginServer stop signal sent."
  fi
else
  echo "LoginServer is not running."
fi
