# 聊天At <Badge type="tip">chat-at</Badge>

字面意思，让你可以在服务器里At别人

### 说明

- 输入格式应该按照 字符串内容<空格>@[人名]<空格>字符串内容
- 人名处填入all可以 @全体成员
- @后所有字符内容都将算作提醒内容
- 被@的会收到标题栏显示和声音提醒

### 配置文件

```yaml
chat-at:
  title-fadein: 10 #标题淡入时间
  title-fadeout: 10 #标题淡出时间
  title-stay: 20 #标题停留时间
  at-template: "{#green}@{player}{#reset}" #at的格式
  at-title_template: "{#green}{player}: {#reset}{message}" #标题提醒内容的模板
  sound: true #是否向目标播放提醒音效
```