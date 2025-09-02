# 自定义Tab栏 <Badge type="tip">tab-menu</Badge>
文：GrassBlock2022

-----

修改原版的Tab栏。每秒刷新一次。

> 注意：这玩意和所有修改Tab的插件冲突

### 配置文件
```yml
    header_ui:
      - '{#yellow} ───────────────────────────────── '
      - '{#yellow}>>{#white} {msg#welcome-header} {#yellow}<<{#white}'
      - '{msg#player}    {msg#max-player}'
    footer_ui:
      - ' '
      - '{msg#tps}   {msg#mspt}   {msg#ping}'
      - '{msg#date}'
      - '{#yellow} ───────────────────────────────── '
```
### 语言文件
预设文字:
```yaml
    welcome-header: '欢迎来到{#aqua}{#server-name}{#reset}服务器!'
    tps: '{#gray}TPS: {#white}{#tps}{#white}/20'
    mspt: '{#gray}MSPT: {#white}{#mspt}{#white}'
    ping: '{#gray}Ping: {#white}{#ping}'
    player: '{#gray}当前在线: {#white}{#player}'
    max-player: '{#gray}最大: {#white}{#max_player}'
    date: '{#gray}日期/时间: {#white}{#date}'
    website: '{#gray}官网: {#aqua}{#underline}{#server_website}'