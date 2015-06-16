@ECHO off
TITLE Aion Lightning - Game Server Panel
:MENU
CLS
ECHO.
ECHO   ^*--------------------------------------------------------------------------^*
ECHO   ^|                    Aion Lightning - Game Server Panel                    ^|
ECHO   ^*--------------------------------------------------------------------------^*
ECHO   ^|                                                                          ^|
ECHO   ^|    1 - Development                                       4 - Quit        ^|
ECHO   ^|    2 - Production                                                        ^|
ECHO   ^|    3 - Production X2                                                     ^|
ECHO   ^|                                                                          ^|
ECHO   ^*--------------------------------------------------------------------------^*
ECHO.
SET /P OPTION=Type your option and press ENTER: 
IF %OPTION% == 1 (
SET MODE=DEVELOPMENT
SET JAVA_OPTS=-Xms1280m -Xmx1280m -XX:MaxHeapSize=1280m -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y,suspend=n
CALL StartGS.bat
)
IF %OPTION% == 2 (
SET MODE=PRODUCTION
SET JAVA_OPTS=-Xms1536m -Xmx1536m -server
CALL StartGS.bat
)
IF %OPTION% == 3 (
SET MODE=PRODUCTION X2
SET JAVA_OPTS=-Xms3072m -Xmx3072m -server
CALL StartGS.bat
)
IF %OPTION% == 4 (
EXIT
)
IF %OPTION% GEQ 5 (
GOTO :MENU
)