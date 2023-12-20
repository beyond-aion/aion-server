![Aion 4.8 Banner](https://github.com/beyond-aion/aion-server/assets/1169307/494205be-399a-4e2e-8435-1f0774d92262)
# Aion 4.8 Server Emulator

## Building
All projects are Maven based. The game server, login server and chat server can be built using `mvn package` from the root directory.  
The resulting zip files in each server's target folder can be deployed on any system with a suitable JDK and access to a MySQL server.  

## Configuration
The servers can be run with the default config after setting up the databases via the init file in each server's sql folder (default DB names 
and users can be found in `config/network/database.properties`).  
To whitelist the game server connection to the login server, enter its ID, IP mask and password in the `gameserver` table of the login server
database.  
If you want to change some configs, it's recommended to create the files `config/mycs.properties` (chat server), `config/mygs.properties` (game 
server) and `config/myls.properties` (login server) and put all your custom properties in there. These take precedence over the standard 
*.properties files and will not be modified when updating the server.  
Finally, don't forget to use a No-IP patched game client, otherwise it will not connect to your IP.

## Developing
Eclipse users can import each project directly as an Eclipse project, which will automatically set up formatting rules.  
If you're using another IDE, import the root directory as a Maven project. To import formatting rules in IntelliJ, install the "Eclipse Code
Formatter" plugin and configure it with `game-server/.settings/org.eclipse.jdt.core.prefs` and `game-server/.settings/org.eclipse.jdt.ui.prefs`.
Import order might not be followed if auto import is active, so deactivate it if necessary.  
To start a server, create a run/debug configuration with the `*Server` class as the main class. The chat server for example starts from
`ChatServer.java`. The working directory needs to be set to the module directory (`$MODULE_WORKING_DIR$` in IntelliJ).   
If your IDE compiles very slowly, the compiler likely needs more memory. The option is called "Build process heap size" in IntelliJ.