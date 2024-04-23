@ECHO off
:: Run in Windows Terminal if available
IF "%WT_SESSION%" == "" wt %0 2>nul && EXIT
TITLE Aion Emu - Game Server

:START
CLS
JAVA -Xms1024m -Xmx2560m -XX:+UseNUMA -DconsoleEncoding=CP850 -cp "libs/*" com.aionemu.gameserver.GameServer
IF %ERRORLEVEL% EQU 0 GOTO END
IF %ERRORLEVEL% EQU 2 GOTO START
ECHO.
ECHO Game server has terminated abnormally!
ECHO.
PAUSE >nul
EXIT

:END
ECHO.
ECHO Game server has shut down
ECHO.
PAUSE >nul
EXIT