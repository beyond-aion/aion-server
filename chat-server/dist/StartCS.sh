#!/bin/bash
#=====================================================================================
# Usage:        StartCS.sh [jvmArgs]
# Parameters:   jvmArgs
#                   additional arguments to the JVM process starting the server
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
  java -Xms72m -Xmx72m -XX:+UseNUMA $@ -cp "libs/*" com.aionemu.chatserver.ChatServer &
  echo $! > chatserver.pid
  # put job in foreground again (wait for termination) and return exit code
  fg %+
  return $?
}

loop() {
  echo "ChatServer restart loop is active."
  while true; do
    run_cs $@
    err=$?
    case ${err} in
      0) # CS exit code: shutdown
        echo "ChatServer stopped."
        break
        ;;
      1) # CS exit code: critical config or build error
        >&2 echo "ChatServer stopped with a fatal error."
        break
        ;;
      2) # CS exit code: restart
        echo "Restarting ChatServer..."
        ;;
      130) # CS process was stopped (Ctrl+C)
        echo "ChatServer process was stopped."
        break
        ;;
      137) # CS process was killed
        echo "ChatServer process was killed."
        break
        ;;
      *) # other
        >&2 echo "ChatServer has terminated abnormally (code: ${err}), restarting in 5 seconds."
        sleep 5
        ;;
    esac
  done
  echo "ChatServer restart loop has ended."
  exit $err
}

cd `dirname $(readlink -f $0)`
loop $@
