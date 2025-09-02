# 模块
文: GrassBlock2022

-----

模块，即Quark提供功能的基本形式。
每个功能在插件中被划分为一个个模块，通过统一的代理框架将内容注册到Quark核心。
每个模块之间相互隔离，因此任意一个模块出现异常均不会影响整体安全。

### 管理模块
- ```/quark module enable [id]``` 启用一个模块
- ```/quark module disable [id]``` 停用一个模块
- ```/quark module reload [id]``` 重载一个模块
- ```/quark module enable-all``` 启用全部模块
- ```/quark module disable-all``` 停用全部模块
- ```/quark module reload-all``` 重载全部模块
- ```/quark module list-all``` 列出全部模块

### 索引

#### Automatic `quark-automatic`

- GarbageCleaner: periodically clean dropped items in world.
- AutoSave: replace minecraft's auto save policy.
- VMGarbageCleaner: periodically call `System.gc()`method to clean JVM garbage.

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
- WorldEditSelectionRenderer[Deprecated]: render your WorldEdit session.

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
- PositionAlign: align your position/rotation to angle.

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

#### Lobby `quark-lobby` [DefaultDisabled]

- BackToSpawn: send player back to spawn everytime they join server.
- DefaultInventory: set player's inventory to preset inventory content.
- MapProtect: defend players from destroying lobby map.
- PlayerProtect: send player back when they go outside from world.

#### ProxySupport `quark-proxysupport` (Need Proxy Network) (Not Recommended)

- BungeeConnectionProtect: verify bc/waterfall pointed to it is valid or not.
- ChatSync: broadcast chat message to all server.
- MCSMDynamicInstance: allow a server to start any mcsm instance.
- ProxyPing: add player-proxy ping to make the ping real.
- ServerStatementObserver: broadcast message when a server is online.

#### Web `quark-web` (Need HTTP Port) [Experimental]

- ServerQueries: enable server queries using HTTP api.
- AccountActivation: bind an E-mail to a player.