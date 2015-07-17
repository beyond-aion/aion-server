#!/bin/sh
if [ -e chatserver.pid ]
then
  cspid=`cat chatserver.pid`
  kill ${cspid}
  echo "ChatServer stop signal sent."
else
  echo "ChatServer is not running."
fi