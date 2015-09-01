#!/bin/bash
#=====================================================================================
# Usage:        StartLS.sh [-noloop]
# Parameters:   -noloop
#                   disables restart loop service (server restart command won't work)
# Description:  Starts LS.
#=====================================================================================

run_ls() {
  if [ ! -f ./libs/AL-Login.jar ]; then
    echo "Exiting script: LoginServer not found. Please check your working directory."
    exit 1
  elif [ -f loginserver.pid ] && ps -p `cat loginserver.pid` > /dev/null 2>&1; then
    echo "Exiting script: LoginServer is already running."
    exit 2
  fi
  java -Xms32m -Xmx32m -server -ea -Xbootclasspath/p:./libs/jsr166.jar -cp ./libs/*:AL-Login.jar com.aionemu.loginserver.LoginServer > /dev/null 2>&1 &
  lspid=$!
  echo ${lspid} > loginserver.pid
  echo "LoginServer is starting now."
  if [ $# -gt 0 ] && [ $1 = "exitcode" ]; then
    # wait for LS termination and return exit code
    wait ${lspid}
    return $?
  fi
}

loop() {
  echo "LoginServer restart loop is active."
  while true; do
    run_ls "exitcode"
    err=$?
    case ${err} in
      0) # LS exit code: shutdown
        echo "LoginServer stopped."
        break
        ;;
      1) # LS exit code: critical config or build error
        echo "LoginServer stopped with a fatal error."
        break
        ;;
      2) # LS exit code: restart
        echo "Restarting LoginServer..."
        ;;
      *) # other
        echo "LoginServer has terminated abnormally, restarting in 5 seconds."
        sleep 5
        ;;
    esac
  done
  echo "LoginServer restart loop has ended."
}

if [ $# -gt 0 ] && [ $1 = "-noloop" ]; then
  run_ls
else
  # init restart loop as background process
  loop &
fi
