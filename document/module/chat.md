# 聊天

文：GrassBlock2022

-----
为服务器带来一些聊天上的修改，例如自定义聊天组件

### ChatAt | 聊天@

字面意思，让你可以在服务器里@别人

**模块id:** ```quark_security:chat_at```

**说明**

- 输入格式应该按照 字符串内容<空格>@[人名]<空格>字符串内容
- 人名处填入all可以 @全体成员
- @后所有字符内容都将算作提醒内容
- 被@的会收到标题栏显示和声音提醒

**配置文件:**
```yaml
  chat_at:
    title_fadein: 10 #标题淡入时间
    title_fadeout: 10 #标题淡出时间
    title_stay: 20 #标题停留时间
    at_template: "{#green}@{player}{#reset}" #at的格式
    at_title_template: "{#green}{player}: {#reset}{message}" #标题提醒内容的模板
    sound: true #是否向目标播放提醒音效
```

