# 爆炸保护 <Badge type='tip'>explosion-defender</Badge>
文:GrassBlock2022

-----

> 恭喜你找到了quark的第一个模块 :D
> 
> ——GrassBlock2022

提供可配置的防爆功能。

### 配置文件

```yaml
  explosion-defender:
    override_explosion: true #是否重新覆盖生成一个假爆炸（不破坏方块）
    record: true #是否记录爆炸事件在文件中
    broadcast: false #是否广播爆炸事件给管理员
```

### 命令

管理爆炸保护区域: `/explosion-whitelist`
- `list` 查看已添加的区域
- `add [name] [world] [x0] [y0] [z0] [x1] [y1] [z1]` 添加一个白名区域
- `remove [name]`移除一个区域
