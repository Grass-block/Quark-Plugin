# CustomLogFormat | 自定义服务端log格式

文: GrassBlock2022

-----

自定义服务端的Log格式，需要log4j支持。

> 本模块仅在Paper和其分支服务端有效。

**模块ID:** ```quark_misc:custom_log_format```

我是不可能把一堆xml扔在yaml里面的，而且我也解析不了。

配置位置：插件文件夹/log.xml

**命令:**

- `/log-format`: 控制log格式
    - `on`: 注入log格式
    - `off`: 恢复log格式
    - `reload`: 从磁盘重载log格式
    - `restore`: 恢复默认log格式