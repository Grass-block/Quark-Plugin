# PermissionManager | 权限管理器
文：GrassBlock2022

-----

提供类似PermissionEx的权限组和单独覆盖一位玩家的权限的功能
(注: 通配符*可以引用该节点下所有子权限)

**模块id:** `quark_security:permission_manager`

**配置文件**
```yaml
#(为避免阅读不便，这里略去了一些基础权限组)
  permission_manager:
    default_group: player #设置默认权限组id
    groups:
      operator: #创建一个组，可以在下面继续填写（赋予操作先允许再去除不允许）
        disallow: #不允许执行的权限
          - minecraft.command.stop
          - minecraft.command.reload
        allow: #允许执行的权限
          - minecraft.command.*
          - worldedit.command.*
```

**命令:**

- `/permission`：管理权限（需要OP）:
  - `group [name] [group]`: 设定一个玩家的权限组
  - `group set [name] [node] [true/false/unset]`: 设定一个玩家的权限
