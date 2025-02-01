### 0.70.06

- [add] DisplaySetting: add `/display-setting` to allow players to disable displays on screen.
- [fix] ChatComponent: Exception when missing SignChangeEvent#line().
- [fix] ChatComponent: lost of text style when re-editing signs.
- [fix] ChatFormat: language key error on mohist platform(remapping worldIds).

### 0.70.03

- [fix] Core: fixed APM network query system.
- [upd] SelfMessage: moved to `quark-utilities` pack.
- [upd] Mail: add `send-direct-to-online-player` option.
- [mov] CommandExec: moved to `quark-commands`
- [mov] SelfMessageCommand: moved to `quark-commands`
- [mov] CommandFunction: moved to `quark-commands`
- [mov] ConsoleCommand: moved to `quark-commands`
- [mov] EntityMotion: moved to `quark-commands`
- [mov] PositionAlign: moved to `quark-commands`
- [mov] ItemCommand: moved to `quark-commands`
- [upd] PlayerNameHeader: changes of header will be announced.
- [fix] CameraMovement: fixed velocity calculate error.
- [fix] CameraMovement: fixed player's gamemode.
- [fix] ChatFormat: fixed vanilla legacy server's player name formatting issue.
- [fix] Core: fixed module status store error.
- [add] IncompleteInstallationDetector: now quark-core will notify to ops if no extension packs installed.

### 0.70.0

- [upd] Framework: set API version to 40.
- [add] Framework: add platform detection for Banner and Youer server.
- [upd] Framework: improved startup performance.
- [upd] Framework: now will show information in Console.
- [rem] Framework: removed BOOTSTRAP_CLASSES stage since it's no longer needed.
- [add] Framework: remade and introduced APM plugin networking.
- [fix] Framework: fix unexpected behavior when console is calling entity selector.
- [fix] Framework: fix unexpected behavior when other entities are expected to get their "locale".
- [fix] AFK: fixed AFK display error.
- [fix] AFK: fixed detection time delta.
- [fix] PlayerNameHeader: fix name-tag display in header.
- [rem] PlayerNameHeader: remove ProtocolNameTags.
- [add] JoinQuitMessage: add configurable "sound" control.
- [rem] NickName: remove for dev.
- [rem] PlayerSkinCustomizer: remove for dev.
- [add] ServerInfo: add /system command so you can check server details.
- [add] CommandExec: add /exec <selector> [command] so you can execute ANY command as target entity.
- [add] Translation: Added en_us translations for quark_content pack.
- [fix] Translation: fixed missing of en_us/quark-display:afk translations
- [add] Freecam: add `anti-cheat` options to prevent in-freecam players.
- [upd] ConsoleCommand: add tab completion.
- [fix] MusicPlayer: fixed autoplay fetch issue.
- [fix] Framework: fix stats page display

### 0.64.15

- [upd] Framework: hot reload on all platforms are sync now.
- [upd] Framework: Language Detection will use cache as first in first time. cause lag for detection but more accurate.
- [upd] Framework: PlayerData will now use UUID to identify player, legacy files will be updated.
- [upd] Framework: All Modules are now using Log4j as logger.
- [upd] MusicPlayer: Always attempt to ignore delay before any first node being played.
- [upd] TabMenu: Default splitLine is longer now.
- [upd] PlayerNameHeader: now use PlayerDataService to store header,legacy file will be auto-updated.
- [upd] AdvancePluginCommand: Injected aliases to /plugins commands like /pl.
- [upd] AdvancePluginCommand: Added hover and clicking infos when listing plugins.
- [upd] AdvancePluginCommand: Plugin manager can now fully remove a plugin and its ref when unloading it. [1.20.1]
- [upd] WESessionSizeLimit: Player Edit operation will be recorded.
- [add] RTP: Added /rtp for random teleporting
- [rem] RealisticSleep: move to beta due to crushes and not-good feedbacks

### 0.64.14

- fix AsyncScheduler using wrong backend
- fix hot reload issue[1.20.1]
- fix view-distance calc exception when player died
- fix we-selection-limit issue when not complete
- fix player-join data loading issue[1.20.1]

### 0.64.13

compatibility: 0.64.10+

[fix]

- Actionbar UI are now sorted.(when they show up both,only high-priority UI will be rendered)

[changes]

- Adding PortableFunctionalBlocks, allow users to open workbench and grindstone without place them.
- Player can open their EnderChest by holding them and right clicking air
- Adding a more detailed ModuleManager and PackageManager listing display with hovered info
- /quark package list and /quark module list can now have search params.
- Remake stats page.
- Added legacy support down to 1.8.8.
- HoverDisplay can now have multiple columns by placing {#return} as separator

### 0.64.12

[fix]

- fixed ChatRenderer default rendering format issue.
- fixed Worldedit selection tracking when not holding wooden_axe as sel-wand tool.
- fixed Tasks auto-finalize issue on Folia/Leaves platform.
- fixed ItemDropSecure lost item when inventory full

[features]

- WESessionRenderer can now use multi render mode(update/off/persistent) and change by player.
- All 3d-box HUD now using Flame particle.
- Using natively Worldedit API for WESessionTracking.
- WESessionSizeLimit can now limit size and stack length.
- WESessionSizeLimit can now record out-bound selecting.
- Added permission(quark.we.size.bypass) for WESessionLimit bypass check.
- ItemDropSecure will no longer work if player inventory is full.
- ChatComponents can now use {translate}key#format1;;format2{;}to build a translatable component.

### 0.64.10

[core]

- adding new configurations API
- unused config will no longer be auto created
- packages should now provide config registry entry
- plugin will now reload on main thread
- adding finalize tasks API
- fixed exception when loading Full jar on renamed core file.

[minecart-controller]

- adding minecart(only normal minecart) movement realistic simulation
- minecart speed will be set to 0 when player entered
- minecart will automatically detect environment and limit its speed when entering slips

[player-name-header]

- name prefix and postfixes will now appear in display name
- name prefix and postfixes will now appear in every minecraft player component(death message...)

[custom-scoreboard]

- adding a BELOW_NAME column(for 1.20.4+ server)

[+nickname] (require protocol lib)

- adding a nickname module[beta]

[hover-display]

- display texts can now have [Space] inside
- display texts can now be saved and reloaded.

[version-check]

- change version calculation method
- fix announce format error when player join.

### 0.63.16

- fixed surrounding-refresh radius check bypass

### 0.63.15

- fixed PermissionManager calculation error
- adding command asserting and prompt
- adding multi-instrument support(experimental) and power support to MusicPlayer
- fix music-play stuttering issue
- fix StopConfirm detect issue
- added 'tpahere' command to TPA
- when a player quit, all the TPARequest on him/her will be cleared
- fixed tab-menu space problem(restore quark-display config)

### 3.62.23

- 新增账户控制功能
- 新增HTTP查询API
- 新增[一言]提供的每日一句
- 新增SMTP服务支持

### 3.62.18

- 新增高级权限控制（交互权限）
- [1.20+]新增@前缀的tab补全

### 3.62.15

- 修复了聊天组件对于JSON识别的错误
- 修复了部分placeholder的识别错误
- 引入自定义PDC物品元数据
- 核心架构改动，部分命令类下放
- 支持Paper1.20.6

### 3.62.04

- 文件结构更改
- 新增群组跨服的传送音效
- 新增群组跨服的传送目标和来源
- 新增Fast-Boot快速启动机制

### 3.63.18

- Fixed ServiceRegistry unregister failure.
- Fixed CommandFunction and PermissionManager
- PermissionManager now start to use external assets to load tags.
- Implement new Language System.