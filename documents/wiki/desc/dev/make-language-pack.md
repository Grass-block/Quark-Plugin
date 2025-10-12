# 制作语言包
文: GrassBlock2022

-----

语言包有点类似资源包，可以向quark-core注入你自己的语言文件。它们的优先级比用户配置项要高一些。

### 描述符

就像资源包一样，每个语言包会有一个自己的描述符文件，它们都叫pack.json。
```json
{
  "scheme_uid": "CB723E2A-873D-5435-8CF2-9DEC5D09BDBD",//固定ID
  "id": "07-zh_meme", //你的语言包唯一ID
  "display": {
    "zh_cn": "梗体中文汉化包", //不同语言下你的语言包显示名称
    "zh_meme": "tmd体中文"
  },
  "author": ["TWSFTS_07007"], //作者列表
  "licence": "All Rights Reserved", //使用的协议
  "url": "example.org" //网址
}
```

### 包内容
语言包中的文件必须以规定的形式命名。
```
<包ID>.<语言ID>.yml
```
文件结构与用户配置的没有区别，你也可以参考template来编辑。
所有语言文件只需要堆在压缩包根目录就行