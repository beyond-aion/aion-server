#!/bin/bash
#=====================================================================================
# Usage:        StopGS.sh [-f]
# Parameters:   -f
#                   forces termination (may lead to loss of data!)
# Description:  Performs a shutdown of GS if running.
#=====================================================================================

if  [ ! -f gameserver.pid ] && [ ! -d libs ]; then
  echo "GameServer PID file not found. Please check your working directory."
  exit 1
elif [ -f gameserver.pid ] && ps -p `cat gameserver.pid` > /dev/null 2>&1; then
  gspid=`cat gameserver.pid`
  if [ $# -gt 0 ] && [ $1 = "-f" ]; then
    kill -KILL ${gspid}
    echo "GameServer was killed."
  else
    kill ${gspid}
    echo "GameServer stop signal sent."
  fi
else
  echo "GameServer is not running."
fi
