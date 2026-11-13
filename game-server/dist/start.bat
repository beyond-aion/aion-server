@ECHO off
:: Run in Windows Terminal if available
IF "%WT_SESSION%" == "" wt %0 2>nul && EXIT
TITLE Aion Emu - Game Server
CHCP 65001 >nul
SET THREAD_LOG=%%(){}

:START
CLS
:: AOT cache for a faster start: created on the first shutdown, recreated after updates (see AotCacheGuard)
SET AOT=cache\gs.aot
IF EXIST %AOT%.outdated DEL /Q %AOT% %AOT%.outdated
IF EXIST %AOT% (SET AOT_OPTION=-XX:AOTCache=%AOT%) ELSE (SET AOT_OPTION=-XX:AOTCacheOutput=%AOT%)
JAVA -Xms1024m -Xmx2560m -XX:+UseNUMA -XX:+UseCompactObjectHeaders %AOT_OPTION% -cp "libs/*" com.aionemu.gameserver.GameServer
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