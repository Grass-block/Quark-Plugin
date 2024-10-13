# 权限管理器 <Badge type="tip">permission-manager</Badge>
文：GrassBlock2022

-----

提供类似PermissionEx的权限组和单独覆盖一位玩家的权限的功能
(注: 通配符*可以引用该节点下所有子权限)

每个玩家只有一个所属的权限组,但是可以有很多权限标签。
每个标签都可以定义一些权限,修改会进行叠加。
`--operatior`表示默认OP权限组,`--player`表示默认玩家权限组。

修改优先级:(自上而下)s
- PermissionItem(权限节点)
- PermissionTag(权限标签)
- PermissionGroup(权限组)



### 配置
#### 权限组 `/quark/assets/permission/[name].yml`
```yml
tags:
  creative:
    - '+minecraft.command.tp'
    - '+minecraft.command.gamemode'
    - '-quark.fly.*'
  other:
      - '-example.permission.value'
```
#### 权限标签 `/quark/assets/permission/[name].yml`
```yml
groups:
  creative:
    - '+minecraft.command.tp'
    - '+minecraft.command.gamemode'
    - '-quark.fly.*'
  other:
    - '-example.permission.value'
```

### 命令

管理权限: `/permission`
- `group [name] [group]`: 设定一个玩家的权限组
- `group set [name] [node] [true/false/unset]`: 设定一个玩家的权限
- `group tag [name] [add|remove] [name]`: 设定一个玩家的权限标签
  
