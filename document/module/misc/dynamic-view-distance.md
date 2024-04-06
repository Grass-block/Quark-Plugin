# DynamicViewDistance | 动态渲染距离
文：GrassBlock2022

-----
> 本模块仅在Paper和其分支服务端有效。

这个模块其实并没有调整玩家客户端的设置，它只决定了服务端发送的数据量，即视野上限。

**模块ID:** ```quark_misc:dynamic_view_distance```

**配置文件:**

```yaml
  dynamic_view_distance:
    max: 32 #最大可设置范围(2 ~ x;x<=32)
```

**命令:**
- ```/view-distance [玩家] [距离](unset)```: 设置玩家的渲染距离

