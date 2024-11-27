<!--
**Important Information:**

**Due to the lead developer (GrassBlock2022) needing to start preparing for the high school academic proficiency exams arranged by the Chinese Ministry of Education starting from September 2, 2024, there will be no key(huge) updates to this project for the next 1-2 years. This project will be active after 2026/6/10**
-->

<div align="center" id="readme-top">

<h2 align="center">Quark-Plugin</h2>

An 'Atomic' designed server plugin aimed to cover anything you need.

[Explore docs](https://wiki.atforever.world/quark/) | [Report issue](https://github.com/Grass-block/Quark-Plugin/issues)

![MCVersion](https://img.shields.io/badge/minecraft-1.17.1_--_1.20.4-3366CC?style=for-the-badge&logoColor=blue&labelColor=29355F)
![Java17+](https://img.shields.io/badge/java-17+-009B98?style=for-the-badge&logoColor=blue&labelColor=29355F)
![MohistCompat](https://img.shields.io/badge/Mohist-Compatible-AD3333?style=for-the-badge&logoColor=blue&labelColor=29355F)

</div>

**This project is still under development and inconsistency.**

### Description

#### What is the aim of this plugin?

- A management plugin...
- A set of toolbox and some randomly features...
- A set of game features add and tweaks...

This is an essential plugin suite for Spigot/Paper/Folia/Mohist servers, 
including more than 90 modules that provides countless features for servers of any scale!
This plugin also includes several performance enhancements and fixes.
From management to display,game features to security, 
these contents will improve your server from every aspect.

#### What can you do with it?

- create a SMP server.
- create a CMP server with WorldEdit and CoreProtect.
- create a Lobby server with Citizens and sign-in plugins.

All the features are independently and composable.
you can customize its function by enable or disable modules,
add or remove extension packs,
or even make your own pack through sdk pack. 

#### What features does it have?

- MultiPlatform support: supports basically every Bukkit-Implemented Server(see below)
- Fast: initialize on less than 600ms, support command-line hot reload.
- Modularized: all features are separately toggleable.

#### What relationship does it have to that `quark` mod?

Basically, nope :D<br/>
This is a server plugin. Although it has a same design pattern as that mod,
but this project have no relationship to it.<br>
By the way, Quark Mod is also a good work.


### Installation

Since Modrinth has changed their download page, you should remember:

- The primary file 'quark-core' is just a library and loader (or a depend).
- Features are classified implementated in sub-packs(listed below).
- The bundle file 'quark-bundler' includes a core and sub-packs, conflict with all file shown up.
- Please do not rename files as possible as you can.(althought it has no effect)
- Versions showed in downloads are inaccurate. all plugin version are compat in [1.8-1.20]. detailed informations about version compat can be found below.


### Features

<details>
<summary>Pack: quark-base [Server commons and utilities]</summary>

#### Chat `quark-chat`

- ChatAt: enables you to use @someone in chatline
- ChatComponent: allows user to custom their text to minecraft rich text in chatline and signs.
- ChatFilter: use RegExp to filter some bad words.
- ChatGPT[Experimental]: chat with ChatGPT and NewBing with no context.
- ChatMute: mute someone from sending any message.
- ChatReport: replace Mojang's chat report system.
- Hitokoto: send a daily random message using hitokoto lib api.
- Mail: send message to someone whatever he/she is online or not.
- NPCChat: simulate a conversation as npc.
- SelfMessage: a command utility to send message to yourself.

#### Display `quark-display`

- BossbarAnnouncement: display custom information via bossbar.
- ChatAnnounce: send announcements,preset tips and hints in chat.
- ChatFormat: customize your chat format.
- CustomBanMessage: customize ban message format.
- CustomKickMessage: customize kick message format.
- CustomMotd: customize server MOTD message and icon.
- CustomScoreboard: display custom information via scoreboard.
- JoinQuitMessage: custom join-quit game message(available for waterfall proxy)
- PlayerNameHeader: give player a displayed rank as a prefix of he/she's display name.
- TabMenu: customize your tab menu display.
- WelcomeMessage: send a message to a player which he/she join server in first time.
- WorldEditSelectionRenderer: render your WorldEdit session.

#### Management `quark-management`

- AdvancedBan: customize ban time and reason,broadcast when a player is banned.
- AdvancedPluginCommand: allows /plugins to load,reload,enable,disable plugins.
- KickOnReload: kick all player when server reload.
- Maintenance: provides a mantenance mode which only allows op to join.
- StopConfirm: same as confirm requirement when reload on paper server.

#### Security `quark-security`

- AdvancedPermissionControl: create more permissions(modify world etc.)
- ExplosionDefender: defend explosions from destroying world.
- IPDefender: check your IP address location and warn on change.
- ItemDefender: warn or takeaway player's item on blacklist.
- PermissionManager: manage player's permission in groups and nodes.
- ProtectionArea: create protected area where non-op players cannot interact.
- WorldEditSectionSizeDefender: defend players from creating and operating on to-big WorldEdit selections.

#### Utilities `quark-utilities`

- BlockUpdateLocker: Lock all block updates in your world.
- Calculator: do simple calculating in chat line.
- CameraMovement: simulate player to a camera on a path.
- CommandFunction: easier way to create a sequence of commands and accept argument input.
- CommandTabFix: filter the tab options send to client base on their known input.
- ConsoleCommand: run command as console.
- CustomLogFormat[Deprecated]: custom your log format in console and log file.
- DynamicViewDistance: allow users to dynamically change their server view distance.
- ForceSprint: keep a player's sprinting status.
- ItemCommand: bind a command to an item where they could be executed through interacting that item.
- ItemCustomName: use a name template in server language file on item.
- PlayerPingCommand: query the player's ping using a command.
- PlayerPositionLock: lock a player's position.
- SurroundingRefresh: force re-send surrounding blocks to client to prevent ghost blocks.

#### Automatic `quark-automatic`

- GarbageCleaner: periodically clean dropped items in world.
- AutoSave: replace minecraft's auto save policy.
- VMGarbageCleaner: periodically call `System.gc()`method to clean JVM garbage.

</details>

<details>
<summary>Pack: quark-game [Game Tweakings and features]</summary>

#### Contents `quark-contents`

- CustomRecipe: add custom recipes using simple yml.
- Elevator: add an elevator block same as OpenBlocks mod.
- Hats: put anything on helmet slot using command.
- MinecartController: allow users to control their minecart.
- MusicPlayer: play MIDI as note block music.
- StairSeat: allows player to sit on stairs.
- TPA: teleport request.
- Waypoint: add public or private waypoints and warp between them.

#### Tweaks `quark-tweaks`

- CropClickHarvest: right click to automatically harvest a crop and replant it.
- DispenserBlockPlacer: allow dispenser to place blocks and use tools to break block.
- DoubleDoorSync: double doors will sync when open or close by player.
- FlySpeedModifier: allows player to fly in every mode if they have permission, modify their flyspeed.
- FreeCam: allow players to have a free camera.
- PortableShulkerBox: allow player to open shulker box on their hand.
- RealisticSleep: allow players to sleep everywhere, implementing SmoothSleep features.
- VeinMiner: chain-mine logs and ores.
</details>

<details>
<summary>Pack: quark-proxy [Proxy networking and syncs]</summary>

#### Lobby `quark-lobby` [Disabled]

- BackToSpawn: send player back to spawn everytime they join server.
- DefaultInventory: set player's inventory to preset inventory content.
- MapProtect: defend players from destroying lobby map.
- PlayerProtect: send player back when they go outside from world.

### ProxySupport `quark-proxysupport` (Need Proxy Network) (Not Recommended)

- BungeeConnectionProtect: verify bc/waterfall pointed to it is valid or not.
- ChatSync: broadcast chat message to all server.
- MCSMDynamicInstance: allow a server to start any mcsm instance.
- ProxyPing: add player-proxy ping to make the ping real.
- ServerStatementObserver: broadcast message when a server is online.
</details>

<details>
<summary>Pack: quark-web [HTTP service provider]</summary>

#### Web `quark-web` (Need HTTP Port) [Experimental]

- ServerQueries: enable server queries using HTTP api.
- AccountActivation: bind an E-mail to a player.

</details>

### Compatibility

> We STRONGLY recommend you to use paper based server to enable full features.
> it could work on other platform, but who knows what will happen? (since lazy GrassBlock2022 never test them)

> Versions showed in versions page are just for placeholder. all versions can work in [1.13-1.20]

| Platform | Features | Interactive Text | Support | Description                    |
|----------|----------|------------------|---------|--------------------------------|
| Paper    | Full     | Full             | Full    | Recommended                    |
| Spigot   | Most     | Full             | BugFix  |                                |
| Bukkit   | Few      | No               | Test    |                                |
| Folia    | Most     | Full             | BugFix  | No hot reload                  |
| Mohist   | Few      | No               | Test    | UnexpectedBlockChange not sync |

<hr/>
<div align="center">

#### Quark-Plugin
A open project by GrassBlock2022, owning by @ATCraftMC 2020-2024
</div>
