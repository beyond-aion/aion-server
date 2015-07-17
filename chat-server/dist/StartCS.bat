@ECHO off
TITLE Aion-Lightning - Chat Server Console

:START
CLS
JAVA -Xms32m -Xmx32m -server -cp ./libs/*;AL-Chat.jar com.aionemu.chatserver.ChatServer
SET CLASSPATH=%OLDCLASSPATH%
IF ERRORLEVEL 2 GOTO START
IF ERRORLEVEL 1 GOTO ERROR
IF ERRORLEVEL 0 GOTO END

:ERROR
ECHO.
ECHO Aion-Lightning - Chat Server has terminated abnormaly!
ECHO.
PAUSE
EXIT

:END
ECHO.
ECHO Aion-Lightning - Chat Server is terminated!
ECHO.
PAUSE
EXIT