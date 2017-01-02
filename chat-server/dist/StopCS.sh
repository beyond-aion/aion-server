#!/bin/bash
#=====================================================================================
# Usage:        StopCS.sh [-f]
# Parameters:   -f
#                   forces termination (may lead to loss of data!)
# Description:  Performs a shutdown of CS if running.
#=====================================================================================

dir=$(dirname $0)
pidfile=${dir}/chatserver.pid
if [ -f $pidfile ] && ps -p `cat $pidfile` > /dev/null 2>&1; then
  pid=`cat $pidfile`
  if [ $# -gt 0 ] && [ $1 = "-f" ]; then
    kill -KILL $pid
    echo "ChatServer was killed."
  else
    kill $pid
    echo "ChatServer stop signal sent."
  fi
else
  echo "ChatServer is not running."
fi
