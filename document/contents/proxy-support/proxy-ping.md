# 集群Ping读取

### 描述

通过覆盖自带的查询系统支持下游服务器查询玩家到集群的链接延迟，而不是只读取集群到下游服务器的延迟。

> 需要安装Quark-Proxy-Waterfall插件，需要全服组网环境。

### PlaceHolder

- `ping`:[覆盖]默认查询玩家Ping
- `ping-value`:[覆盖]默认查询玩家Ping数值

### PluginMessage:

- `proxy-ping:update`:强制刷新玩家的ping（向集群发送查询请求）

### 配置项

- `interval`:自动刷新ping(重新查询)的间隔(秒)