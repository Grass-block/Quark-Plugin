# 爆炸保护
文:GrassBlock2022

-----

这个模块有着2年的历史:D

提供可配置的防爆功能。

**模块id:** `quark_security:explosion_defender`

**配置文件:**

```yaml
  explosion_defender:
    override_explosion: true #是否重新覆盖生成一个假爆炸（不破坏方块）
    record: true #是否记录爆炸事件在文件中
    broadcast: false #是否广播爆炸事件给管理员
```

**命令:**

- `/explosion-whitelist` 管理爆炸保护区域 (需要OP)
    - `list` 查看已添加的区域
    - `add [name] [world] [x0] [y0] [z0] [x1] [y1] [z1]` 添加一个白名区域
    - `remove [name]`移除一个区域
