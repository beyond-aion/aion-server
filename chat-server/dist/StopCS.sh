#!/bin/bash
#=====================================================================================
# Usage:        StopCS.sh [-f]
# Parameters:   -f
#                   forces termination (may lead to loss of data!)
# Description:  Performs a shutdown of CS if running.
#=====================================================================================

if  [ ! -f chatserver.pid ] && [ ! -f ./libs/AL-Chat.jar ]; then
  echo "ChatServer PID file not found. Please check your working directory."
  exit 1
elif [ -f chatserver.pid ] && ps -p `cat chatserver.pid` > /dev/null 2>&1; then
  cspid=`cat chatserver.pid`
  if [ $# -gt 0 ] && [ $1 = "-f" ]; then
    kill -KILL ${cspid}
    echo "ChatServer was killed."
  else
    kill ${cspid}
    echo "ChatServer stop signal sent."
  fi
else
  echo "ChatServer is not running."
fi
