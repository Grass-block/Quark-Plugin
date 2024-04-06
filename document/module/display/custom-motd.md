# CustomMotd | 自定义Motd

-----

引入随机MOTD，格式化，MOTD图片。

> 注意：本模块和所有motd修改插件冲突。

**模块id:** ```quark_display:custom_motd```

**语言文件:**
```yml
  custom_motd:
    motd_command: '{global#sys_info}Quark生成的服务器MOTD信息: '
    toc_pre: "{color(blue)}#2024龙年大吉 {color(white)}| {color(gray)}"
    motd_title:  #motd第1行
      - '{color(purple)}{#server-name} Server  {color(red)}[{color(white)}BE/JE 1.12-1.20{color(red)}}]'
    motd_subtitle: #motd第2行
      - '{msg#toc_pre}今天你吃山竹醇了吗?'
      - '{msg#toc_pre}欢迎 :D'
      #...
```

**命令:**
- `/motd`:测试你的motd
