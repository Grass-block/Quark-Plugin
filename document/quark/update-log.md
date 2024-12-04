### 0.64.15

- [Core] hot reload on all platforms are sync now.
- [Core] Language Detection will use cache as first in first time. this will cause lag for detection but reduce chance of
  detecting wrong language.
- [Core] PlayerData will now use UUID to identify player, legacy files will be updated.
- [Core] All Modules are now using Log4j as logger.
- [MusicPlayer] Always attempt to ignore delay before any first node being played.
- [TabMenu] Default splitLine is longer now.
- [PlayerNameHeader] now use PlayerDataService to store header,legacy file will be auto-updated.
- [AdvancePluginCommands] Injected aliases to /plugins commands like /pl.
- [AdvancePluginCommands] Added hover and clicking infos when listing plugins.
- [AdvancePluginCommands] Plugin manager can now fully remove a plugin and its ref when unloading it. [1.20.1]

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