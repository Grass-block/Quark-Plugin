# 物品黑名单 <Badge type="tip">item-defender</Badge>
文: GrassBlock2022

-----

你可以ban掉一些物品，或者是在玩家使用一些物品时给出警告。
同样的，我们提供了信息记录和广播。

### 配置文件:

```yaml
  item_defender:
    record: true #是否记录
    broadcast: false #是否广播给管理员
    op-ignore: true #OP是否可以豁免
    illegal-list: #没收的列表(注: 其中填写 Minecraft ID)
      - "__placeholder__"
    warning-list: #发出警告的列表
      - "tnt"
      - "flint_and_steel"
      - "lava_bucket"
```