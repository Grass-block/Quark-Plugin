# AdvancedPluginCommand | 高级plugins命令
文：GrassBlock2022

-----

修改原版的/plugins命令，使之拥有加载和卸载以及查看信息等高级功能。

>注意：由于API更改，加载和重载插件只有paper1.18和以下平台有效。

**模块ID:** ```quark_misc:advanced_plugin_command```

**命令:**
- `/plugins`: 管理你的插件（需要op）
  - `list`: 查看所有插件
  - `info [name]`: 显示一个插件的详细信息
  - `load [file]`: 从插件文件夹加载一个插件
  - `unload [name]`: 卸载一个插件
  - `reload [name]`: 重载一个插件