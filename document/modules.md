# 模块

文: GrassBlock2022

-----

模块，即Quark提供功能的基本形式。
每个功能在插件中被划分为一个个模块，通过统一的代理框架将内容注册到Quark核心。
每个模块之间相互隔离，因此任意一个模块出现异常均不会影响整体安全。

### 管理模块
- ```/module enable [id]``` 启用一个模块
- ```/module disable [id]``` 停用一个模块
- ```/module reload [id]``` 重载一个模块
- ```/module enable-all``` 启用全部模块
- ```/module disable-all``` 停用全部模块
- ```/module reload-all``` 重载全部模块
- ```/module list-all``` 列出全部模块

### 模块列表

所有模块的描述和使用说明按照类别进行了分组。

- [安全](/course/quark/module/security.md)
- [信息](/course/quark/module/display.md)
- [游戏](/course/quark/module/game.md)
- [聊天](/course/quark/module/chat.md)
- [其他](/course/quark/module/misc.md)
- [网络](/course/quark/module/web.md)