#!/bin/bash
#=====================================================================================
# Usage:        StopLS.sh [-f]
# Parameters:   -f
#                   forces termination (may lead to loss of data!)
# Description:  Performs a shutdown of LS if running.
#=====================================================================================

if  [ ! -f loginserver.pid ] && [ ! -f ./libs/AL-Login.jar ]; then
  echo "LoginServer PID file not found. Please check your working directory."
  exit 1
elif [ -f loginserver.pid ] && ps -p `cat loginserver.pid` > /dev/null 2>&1; then
  lspid=`cat loginserver.pid`
  if [ $# -gt 0 ] && [ $1 = "-f" ]; then
    kill -KILL ${lspid}
    echo "LoginServer was killed."
  else
    kill ${lspid}
    echo "LoginServer stop signal sent."
  fi
else
  echo "LoginServer is not running."
fi
