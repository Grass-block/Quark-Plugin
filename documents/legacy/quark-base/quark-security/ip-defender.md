# IP检测 <Badge type='tip'>ip-defender</Badge>
文:GrassBlock2022

-----

> 说明: 由于IP-API存在对于敏感地区检测结果的涉政不当名词使用,
> 因此在中国(大陆和港澳台地区)均使用CSDN的API作为检测结果。
> 由于我们不确定该API的国际连接速度和监测精准度, 在非国内地区我们仍将使用IP-API检测。
> 如您有能力提供世界范围内的高精度快速IP检测，请联系我们。

监测玩家的IP属地，并在检测到变化的时候自动记录并做出操作。

### 配置文件

```yaml
  ip-defender:
    record: true #是否记录IP属地变化
    auto-ban: false #是否自动封禁
    auto-ban-day-time: 0 #封禁的时间，往下为天，时，分，秒
    auto-ban-hour-time: 0
    auto-ban-minute-time: 10
    auto-ban-second-time: 0
```

### 命令
检查自己的IP属地: `check-ip`
- `<无参数>`: 检查当前的IP属地。

### 插件消息API

IP属地变化事件: `ip_change`
- `player` IP属地变化的玩家名称
- `old-ip` 玩家之前的IP
- `new-ip` 玩家最新的IP