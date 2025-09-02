# CommandFunction | 连锁命令执行
文：GrassBlock2022

-----

让玩家执行一串命令。

**模块ID:** ```quark_misc:command_function```

配置文件:
```yaml
  command_function:
    /mirror: #新命令的名字(无需默认的那个斜杠)
      - /copy #需要连锁执行的命令(无需默认的那个斜杠)
      - /flip
      - /paste
```