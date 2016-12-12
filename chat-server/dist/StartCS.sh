#!/bin/bash
#=====================================================================================
# Usage:        StartCS.sh [-noloop]
# Parameters:   -noloop
#                   disables restart loop service (server restart command won't work)
# Description:  Starts CS.
#=====================================================================================

run_cs() {
  if [ ! -d libs ]; then
    echo "Exiting script: ./libs not found. Please check your working directory."
    exit 1
  elif [ -f chatserver.pid ] && ps -p `cat chatserver.pid` > /dev/null 2>&1; then
    echo "Exiting script: ChatServer is already running."
    exit 2
  fi

  echo "ChatServer is starting now."
  # activate job control in this script
  set -m
  # run server as a background job to instantly write PID file
  java -Xms128m -Xmx128m -XX:+TieredCompilation -XX:+UseNUMA -server -ea -cp "libs/*" com.aionemu.chatserver.ChatServer &
  echo $! > chatserver.pid
  # put job in foreground again (wait for LS termination) and return exit code
  fg %+
  return $?
}

loop() {
  echo "ChatServer restart loop is active."
  while true; do
    run_cs
    err=$?
    case ${err} in
      0) # CS exit code: shutdown
        echo "ChatServer stopped."
        break
        ;;
      1) # CS exit code: critical config or build error
        echo "ChatServer stopped with a fatal error."
        break
        ;;
      2) # CS exit code: restart
        echo "Restarting ChatServer..."
        ;;
      137) # CS process was killed
        echo "ChatServer process was killed."
        break
        ;;
      *) # other
        echo "ChatServer has terminated abnormally, restarting in 5 seconds."
        sleep 5
        ;;
    esac
  done
  echo "ChatServer restart loop has ended."
}

cd `dirname $(readlink -f $0)`
if [ $# -gt 0 ] && [ $1 = "-noloop" ]; then
  run_cs
else
  loop
fi
