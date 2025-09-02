# 导航点 <Badge type="tip">waypoint</Badge>
文: GrassBlock2022

-----

提供公共导航点(全服所有玩家可见) 和私有导航点(仅添加者可见)

### 配置文件

```yaml
  waypoint:
    allow_coordinate_add: true #是否允许玩家添加坐标
```


### 权限
- `quark.waypoint.bypass` 默认: op 绕过“坐标添加” 限制
- `quark.waypoint.bypass` 默认: op 允许编辑公共导航点列表

### 命令
- `/waypoint add [name] [world] [x] [y] [z]` 添加一个公共导航点(仅OP可用)
- `/waypoint add [name] @self` 以自己的位置添加一个公共导航点(仅OP可用)
- `/waypoint remove [name]` 移除一个公共导航点(仅OP可用)
- `/waypoint tp [name]`  传送到一个公共导航点
- `/waypoint add-private [name] [world] [x] [y] [z]` 添加一个私有导航点
- `/waypoint add-private [name] @self` 以自己的位置添加一个私有导航点
- `/waypoint remove-private [name]` 移除一个私有导航点
- `/waypoint tp-private [name]` 传送到一个私有导航点