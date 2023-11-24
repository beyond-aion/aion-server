#!/bin/bash
#=====================================================================================
# Usage:        StartGS.sh [jvmArgs]
# Parameters:   jvmArgs
#                   additional arguments to the JVM process starting the server
# Description:  Starts GS.
#=====================================================================================

run_gs() {
  if [ ! -d libs ]; then
    echo "Exiting script: ./libs not found. Please check your working directory."
    exit 1
  elif [ -f gameserver.pid ] && ps -p `cat gameserver.pid` > /dev/null 2>&1; then
    echo "Exiting script: GameServer is already running."
    exit 2
  fi

  echo "GameServer is starting now."
  # activate job control in this script
  set -m
  # run server as a background job to instantly write PID file
  java -Xms1024m -Xmx2560m -XX:+UseNUMA $@ -javaagent:libs/${javaagentlib} -cp "libs/*" com.aionemu.gameserver.GameServer &
  echo $! > gameserver.pid
  # put job in foreground again (wait for termination) and return exit code
  fg %+
  return $?
}

loop() {
  echo "GameServer restart loop is active."
  while true; do
    run_gs $@
    err=$?
    case ${err} in
      0) # GS exit code: shutdown
        echo "GameServer stopped."
        break
        ;;
      1) # GS exit code: critical config or build error
        >&2 echo "GameServer stopped with a fatal error."
        break
        ;;
      2) # GS exit code: restart
        echo "Restarting GameServer..."
        ;;
      130) # GS process was stopped (Ctrl+C)
        echo "GameServer process was stopped."
        break
        ;;
      137) # GS process was killed
        echo "GameServer process was killed."
        break
        ;;
      *) # other
        >&2 echo "GameServer has terminated abnormally (code: ${err}), restarting in 5 seconds."
        sleep 5
        ;;
    esac
  done
  echo "GameServer restart loop has ended."
  exit $err
}

cd `dirname $(readlink -f $0)`
loop $@
