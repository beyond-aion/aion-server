#!/bin/bash
#=====================================================================================
# Usage:        StartGS.sh [-noloop]
# Parameters:   -noloop
#                   disables restart loop service (server restart command won't work)
# Description:  Starts GS.
#=====================================================================================

run_gs() {
  if [ ! -f libs/AL-Game.jar ]; then
    echo "Exiting script: GameServer not found."
    exit 1
  elif [ -f gameserver.pid ] && ps -p `cat gameserver.pid` > /dev/null 2>&1; then
    echo "Exiting script: GameServer is already running."
    exit 2
  fi

  echo "GameServer is starting now."
  # activate job control in this script
  set -m
  # run server as a background job to instantly write PID file
  java  -Xms512m -Xmx2560m -XX:+TieredCompilation -XX:+UseNUMA -server -ea -javaagent:libs/{javaagentlib} -cp "libs/*" com.aionemu.gameserver.GameServer &
  echo $! > gameserver.pid
  # put job in foreground again (wait for LS termination) and return exit code
  fg %+
  return $?
}

loop() {
  echo "GameServer restart loop is active."
  while true; do
    run_gs
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
      137) # GS process was killed
        echo "GameServer process was killed."
        break
        ;;
      *) # other
        echo "GameServer has terminated abnormally, restarting in 5 seconds."
        sleep 5
        ;;
    esac
  done
  echo "GameServer restart loop has ended."
}

cd `dirname $(readlink -f $0)`
if [ $# -gt 0 ] && [ $1 = "-noloop" ]; then
  run_gs
else
  loop
fi
