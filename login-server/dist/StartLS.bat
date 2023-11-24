@ECHO off
TITLE Aion Emu - Login Server Console

:START
CLS
JAVA -Xms48m -Xmx48m -XX:+UseNUMA -DconsoleEncoding=CP850 -cp "libs/*" com.aionemu.loginserver.LoginServer
IF ERRORLEVEL 2 GOTO START
IF ERRORLEVEL 1 GOTO ERROR
IF ERRORLEVEL 0 GOTO END

:ERROR
ECHO.
ECHO Login Server has terminated abnormally!
ECHO.
PAUSE >nul
EXIT

:END
ECHO.
ECHO Login Server has shut down
ECHO.
PAUSE >nul
EXIT