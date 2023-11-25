@ECHO off
:: Run in Windows Terminal if available
IF "%WT_SESSION%" == "" wt %0 2>nul && EXIT
TITLE Aion Emu - Chat Server

:START
CLS
JAVA -Xms72m -Xmx72m -XX:+UseNUMA -DconsoleEncoding=CP850 -cp "libs/*" com.aionemu.chatserver.ChatServer
IF ERRORLEVEL 2 GOTO START
IF ERRORLEVEL 1 GOTO ERROR
IF ERRORLEVEL 0 GOTO END

:ERROR
ECHO.
ECHO Chat Server has terminated abnormally!
ECHO.
PAUSE >nul
EXIT

:END
ECHO.
ECHO Chat Server has shut down
ECHO.
PAUSE >nul
EXIT