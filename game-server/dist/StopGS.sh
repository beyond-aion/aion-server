#!/bin/bash
#=====================================================================================
# Usage:        StopGS.sh [-f]
# Parameters:   -f
#                   forces termination (may lead to loss of data!)
# Description:  Performs a shutdown of GS if running.
#=====================================================================================

dir=$(dirname $0)
pidfile=${dir}/gameserver.pid
if [ -f $pidfile ] && ps -p `cat $pidfile` > /dev/null 2>&1; then
  pid=`cat $pidfile`
  if [ $# -gt 0 ] && [ $1 = "-f" ]; then
    kill -KILL $pid
    echo "GameServer was killed."
  else
    kill $pid
    echo "GameServer stop signal sent."
  fi
else
  echo "GameServer is not running."
fi
