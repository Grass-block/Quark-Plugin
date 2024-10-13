# 配置文件
文: GrassBlock2022

-----

以下内容定义了作为quark核心配置系统的两种文件规范: 配置 和 语言。

### 配置文件
#### 模板
```yaml
config: #根节点，必须是 'config'
  module: #命名空间，在子包配置中它对应一个模块
    default_status: true #kv值
  http_server:
    host: 127.0.0.1
    port: 8125
    backlog: 10
```

#### 命令
- ```/config reload [id]``` 重载一个
- ```/config restore [id]``` 重置一个(恢复到初始状态)
- ```/config reload_all ``` 重载全部
- ```/config restore_all ``` 重置全部(恢复到初始状态)

### 语言文件
#### 模板
```yaml
language: #根节点，必须是 'language'
  global_vars: #命名空间，在子包配置中它对应一个模块
    sys_info: "[{#aqua}{#bold}系统{#white}]{#gray}"
    sys_warning: "[{#yellow}{#bold}系统{#white}]{#gray}"
    sys_error: "[{#red}{#bold}系统{#white}]{#gray}"
```
#### 命令
- ```/language reload [id]``` 重载一个
- ```/language restore [id]``` 重置一个(恢复到初始状态)
- ```/language reload_all ``` 重载全部
- ```/language restore_all ``` 重置全部(恢复到初始状态)
