#!/bin/bash
#=====================================================================================
# Usage:        StartCS.sh [-noloop]
# Parameters:   -noloop
#                   disables restart loop service (server restart command won't work)
# Description:  Starts CS.
#=====================================================================================

run_cs() {
  if [ ! -f ./libs/AL-Chat.jar ]; then
    echo "Exiting script: ChatServer not found. Please check your working directory."
    exit 1
  elif [ -f chatserver.pid ] && ps -p `cat chatserver.pid` > /dev/null 2>&1; then
    echo "Exiting script: ChatServer is already running."
    exit 2
  fi
  java -Xms128m -Xmx128m -ea -Xbootclasspath/p:./libs/jsr166.jar -cp ./libs/*:AL-Chat.jar com.aionemu.chatserver.ChatServer > /dev/null 2>&1 &
  cspid=$!
  echo ${cspid} > chatserver.pid
  echo "ChatServer is starting now."
  if [ $# -gt 0 ] && [ $1 = "exitcode" ]; then
    # wait for CS termination and return exit code
    wait ${cspid}
    return $?
  fi
}

loop() {
  echo "ChatServer restart loop is active."
  while true; do
    run_cs "exitcode"
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
      *) # other
        echo "ChatServer has terminated abnormally, restarting in 5 seconds."
        sleep 5
        ;;
    esac
  done
  echo "ChatServer restart loop has ended."
}

if [ $# -gt 0 ] && [ $1 = "-noloop" ]; then
  run_cs
else
  # init restart loop as background process
  loop &
fi
