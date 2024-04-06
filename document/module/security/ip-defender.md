# IPDefender | IP属地检测
文:GrassBlock2022

-----

监测玩家的IP属地，并在检测到变化的时候自动记录并做出操作。

**模块id:** `quark_security:ip_defender`

**配置文件:**

```yaml
  ip_defender:
    record: true #是否记录IP属地变化
    auto_ban: false #是否自动封禁
    auto_ban_day_time: 0 #封禁的时间，往下为天，时，分，秒
    auto_ban_hour_time: 0
    auto_ban_minute_time: 10
    auto_ban_second_time: 0
```

**API:**

通知ID: `ip_change`

参数: `player` IP属地变化的玩家名称