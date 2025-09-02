# Boss栏公告 <Badge type="tip">bossbar-announcement</Badge>
文：GrassBlock2022

-----

提供在Bukkit服务器上实现BossBar公告功能的模块。
此模块允许管理员通过命令设置自定义的BossBar内容，
并在玩家加入或退出时自动创建或移除相应的BossBar。

### 配置文件

```yaml
bossbar-announcement:
  period: 60 # BossBar更新周期（以秒为单位）
  bar_color: WHITE # BossBar颜色
```

### 命令
管理BossBar公告: `/bossbar-announce`
  - `[content]`: 设置自定义文本内容
  - `none`:清除自定义文本内容
