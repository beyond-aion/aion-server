#!/bin/bash
#=====================================================================================
# Usage:        StartGS.sh [-noloop]
# Parameters:   -noloop
#                   disables restart loop service (server restart command won't work)
# Description:  Starts GS.
#=====================================================================================

run_gs() {
  if [ ! -f ./libs/AL-Game.jar ]; then
    echo "Exiting script: GameServer not found. Please check your working directory."
    exit 1
  elif [ -f gameserver.pid ] && ps -p `cat gameserver.pid` > /dev/null 2>&1; then
    echo "Exiting script: GameServer is already running."
    exit 2
  fi
  java -Xms128m -Xmx1536m -server -ea -javaagent:./libs/{javaagentlib} -cp ./libs/*:AL-Game.jar com.aionemu.gameserver.GameServer > /dev/null 2>&1 &
  gspid=$!
  echo ${gspid} > gameserver.pid
  echo "GameServer is starting now."
  if [ $# -gt 0 ] && [ $1 = "exitcode" ]; then
    # wait for GS termination and return exit code
    wait ${gspid}
    return $?
  fi
}

loop() {
  echo "GameServer restart loop is active."
  while true; do
    run_gs "exitcode"
    err=$?
    case ${err} in
      0) # GS exit code: shutdown
        echo "GameServer stopped."
        break
        ;;
      1) # GS exit code: critical config or build error
        echo "GameServer stopped with a fatal error."
        break
        ;;
      2) # GS exit code: restart
        echo "Restarting GameServer..."
        ;;
      *) # other
        echo "GameServer has terminated abnormally, restarting in 5 seconds."
        sleep 5
        ;;
    esac
  done
  echo "GameServer restart loop has ended."
}

if [ $# -gt 0 ] && [ $1 = "-noloop" ]; then
  run_gs
else
  # init restart loop as background process
  loop &
fi
