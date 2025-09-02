# 自定义Motd <Badge type="tip">custom-motd</Badge>
文: GrassBlock2022

-----

引入随机MOTD，格式化，MOTD图片。

> 注意：本模块和所有motd修改插件冲突。

配置文件位于`/quark/assets/motd.yml`,
motd图标文件位于`/quark/assets/motd.png`.

### 命令
- `/motd text`:测试你的motd
- `/motd reload`:重载motd配置

### 配置文件
```yml
motd:
  motd-title: "{color(aqua,purple)}{#server-name-plain}{;} {version} {#white}> {feature}"
  motd-subtitle: "{#blue}#{tag} {#gold}| {#gray}{splash}"
  splash:
    - 既然说存在即合理，那么不合理是不是不存在？
    - 熬夜，是对昨天做辅助延长线
    - 前程似锦，怪不得我一个穷人买不起
    - 大气中的氮气最多，原来人是薯片
    - "{#gold}{#bold}Technoblade never dies!"
    - 灯塔是陆地的诱饵，发呆是在读空气
    - 时间就像脑子里的水，很多却挤不出来
    - "(-coser)'=罪犯(sinner)"
  feature: "{#green}创造{#gray}·{#green}生存{#gray}·{#green}小游戏"
  version: "{#red}[{#white}1.8-1.20{#red}]"
  tag:
    - "MC15周年庆"
```
