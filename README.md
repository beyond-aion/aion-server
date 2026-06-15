![Aion 4.8 Banner](https://github.com/beyond-aion/aion-server/assets/1169307/494205be-399a-4e2e-8435-1f0774d92262)
<div align="center">

  ![](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fgithub.com%2Fbeyond-aion%2Faion-server%2Fraw%2F4.8%2Fpom.xml&query=%2F%2A%5Blocal-name%28%29%3D%22project%22%5D%2F%2A%5Blocal-name%28%29%3D%22properties%22%5D%2F%2A%5Blocal-name%28%29%3D%22maven.compiler.release%22%5D%2Ftext%28%29&label=Java%20version)
  [![](https://img.shields.io/github/contributors-anon/beyond-aion/aion-server)](https://htmlpreview.github.io/?https://gist.github.com/neon-dev/ce9729bcacaac31f78771b8521512d0a/raw/contributors.html&repo=beyond-aion/aion-server&title=Beyond%20Aion%20Server%20Contributors)
  ![](https://img.shields.io/github/repo-size/beyond-aion/aion-server)

</div>

# Aion 4.8 Server Emulator

This is the server for the MMORPG *Aion: The Tower of Eternity* that we host for our players.  
Our server emulator is intended to be faithful to the original experience of the official servers of the time, but a few custom features have also been implemented to meet the needs of our community.  
You can read more about it here:
<details>
<summary><b>Motivation and features of this server emulator (click to show)</b></summary>

### Motivation
In the early years of the game, from 2009 onwards, there were larger and more organized development teams. When we started, in 2015, those days were long gone.  
The few people with extensive knowledge about the different Aion-Emu[^1] forks know this already. Aion server emulators are barely functional: Many systems have been left unfinished, some have design problems, and incomplete or incorrect template data is the rule, not the exception. What seems to work at first breaks down when you introduce trivial variables such as players playing the game or running the server for more than a few
hours.  

We wanted to change this and create an emulator this wonderful game deserves.  
The base for our project was Aion-Lightning's server for Aion version 4.7.5, which was considered the best emulator at the time. Unfortunately, while it was less buggy than emulators from other groups, it turned out to be in a similarly terrible state.  
Once we opened the server to our community, many more core issues came to light, all of which led to our decision to prioritize bug fixes and optimizations over features or version updates. So we just updated once, to version 4.8 (again with the help of Aion-Lightning's work), and stuck to our plan.  

### Highlights
The following is a very incomplete list of some notable things we have worked on:

#### Custom features
- PvPvE map with increased AP rates and boss spawns
- Solo instance "Eternal Challenge" with a boss using the same skills and tactics like you, based on a deep learning AI
- Customizations to drop lists, QoL improvements, player commands and various PvP and PvE rewards you can read more about [here](https://beyond-aion.com/page/features)

#### Fixes and enhancements
- Fixed geo[^2] related issues like wrong or missing obstacles, incorrect bound calculations, terrain checks, doors, shields, environmental effects, etc.
- Fixed map kicks and other unintended positioning from various skills, some even client-induced (now worked around by the server)
- Implemented missing instances and reworked some
- Fixed hundreds of quests
- Added thousands of missing drops and spawns
- Fixed drop rate calculations and improved the global drop system (removed npc_drop.dat support)
- Fixed duplicate or unintentionally invisible spawns, temporary spawns and added support for temporary spawns in instances and events
- Fixed the event engine and added new features like automatic buffs or config overrides
- Removed, merged or reworked many chat commands, implemented descriptions, common error handling and a permission aware `.help` command
- Implemented true invisibility against anti hide hacks
- Fixed motion validation to combat no-animation hacks
- Fixed many stat and skill related issues with players and NPCs
- Implemented more AI handler events and controls like queueing of skills
- Fixed countless core bugs of various severities, like wrong chance calculations, login problems or even client crashes
- Fixed memory leaks, concurrency related issues and more, so the server no longer needs to be restarted every few hours (runs nicely for months now)
- Development related:
  - Simplified configuration and added support for more data types, including lists and maps
  - Logging improvements: Added support for Discord webhooks and revised all error logging (no missing stack traces anymore or meaningless messages)
  - Optimized startup time and implemented class file caching for even faster startup if handlers haven't been modified since the last start
  - Continuous optimizations for a more light-weight and more efficient server (removal of unnecessary code or dependencies, refactoring, etc.)
  - Regular Java and dependency updates for the latest improvements and new language features

### Outlook

Fast-forward to today and there are still many unfinished tasks, bugs and ideas for improvements. Too many to even try listing them. A project of this size will never be finished by a few people developing it in their spare time.  
Which is fine, because we enjoy working on it.

</details>

**TL;DR**: A lot of work has been put into improving this emulator. Not only for our players, but also for a better experience when developing.

> [!TIP]  
> If you have questions about [contributing](https://github.com/beyond-aion/aion-server/blob/HEAD/.github/CONTRIBUTING.md) or if you are interested in technical discussions about Aion and its server development, you can join our **development-focused Discord**: [![Discord Join Link](https://img.shields.io/badge/Discord-5865f2?logo=discord&logoColor=white)](https://beyond-aion.com/dev-talk)  
> 
> **Please note that we do not provide any support related to hosting your own server, but you can ask the community for help in [Discussions > Q&A](https://github.com/beyond-aion/aion-server/discussions/categories/q-a)**

## Building
This project uses [Maven](https://maven.apache.org/what-is-maven.html) to manage dependencies. The game server, login server and chat server can be
built using `mvn package` from the root directory.  
The resulting zip files in each server's target folder can be deployed on any system with a suitable JDK and access to a MySQL (or MariaDB) server.  

## Configuration
### Server setup
The servers can be run with the default config after initializing the databases with the *.sql file in each server's sql folder (default DB names
and users can be found in `config/network/database.properties`).  
To whitelist the game server connection to the login server, enter its ID, IP mask and password in the `gameserver` table of the login server
database.  
If you want to change some configs, it's recommended to create the files `config/mycs.properties` (chat server), `config/mygs.properties` (game 
server) and `config/myls.properties` (login server) and put all your custom properties in there. These take precedence over the standard 
*.properties files and will not be modified when updating the server.  

### Game client setup
You can download the game client for this version from [here](https://mega.nz/folder/wxMRXZDS#qMsKJlkyYUNp_TQln2EZlg).  
As it blocks connections to non-official servers, it needs to be patched. This can be done by copying
this [version.dll](https://github.com/beyond-aion/aion-version-dll/releases/latest) into the bin32 and bin64 folders of the game client.  
To run the game, create a file called `start.bat` in the game's root directory with the following content:
```batch
start /affinity 7FFFFFFF "" "bin64\AION.bin" -ip:127.0.0.1 -port:2106 -cc:2 -lang:ENG -loginex
```
<sup>The `-lang` parameter accepts any language installed in the l10n folder. The affinity mask ensures that no more than 31 CPU cores are assigned to the
process, as the game client does not support more.</sup>  

## Developing
Import the root directory as a Maven project. If your IDE does not support [EditorConfig](https://editorconfig.org/#pre-installed) natively, install a
plugin for it to ensure a consistent coding style.  
To start a server, create a run/debug configuration with the `*Server` class as the main class. The chat server for example starts from
`ChatServer.java`. The working directory needs to be set to the module directory (`$MODULE_WORKING_DIR$` in IntelliJ).   
If your IDE compiles very slowly, the compiler likely needs more memory. The option is called "Build process heap size" in IntelliJ.


[^1]: [Aion-Emu](https://web.archive.org/web/20100128222712/http://aion-emu.com/) was the first server development project for the game and laid the foundation for all the popular server emulators known today.  
[^2]: Geo or geo data is the common term for collision data parsed from the game client. Collision data for this server is created with our [GeoBuilder](https://github.com/beyond-aion/aion-geobuilder).  