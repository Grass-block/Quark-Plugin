# ChatFormat | 聊天格式化
文：GrassBlock2022

-----

提供对Bukkit服务器聊天消息格式进行自定义的模块。
此模块允许管理员通过配置文件设置聊天消息的模板，
支持Legacy和Paper两种聊天事件的处理。

**模块id:** `quark_chat:chat_format`

**配置文件**
```yaml
chat_format:
  template: "{0} {color(gold)}> {color(gray)}{1}" #{0}是玩家名，{1}是信息
```