# CustomRecipe | 自定义合成配方
文: GrassBlock2022

-----

添加自定义配方。

我知道做这个玩意很智障，但是这东西比数据包简单 ——草方块

**有序合成:**

```yaml
planks_slab_combine: #合成表唯一ID，重复概不负责
  type: "shaped"  #有序合成
  repeat: #样板宏，这些id会被用{#type}组合到id中，进行自动重注册。
    - "oak"
    - "spruce"
    - "acacia"
    - "dark_oak"
    - "birch"
    - "jungle"
  shape: "#;#"  #合成样板 按行用分号分割，每行1-3个字符，映射到调色板ID
  output: "minecraft:{#type}_planks*1" #输出的物品id*数量
  map:
    "#": "minecraft:{#type}_slab" #调色板，不用我多说了吧
```

**无序合成**

```yaml
quartz_split:
  type: "shapeless" #无序合成
  output: "minecraft:quartz*4"
  requirements: #需要的物品
    - "minecraft:quartz_block"
```

**切石机合成:**

```yaml
planks_cutting:
  type: "stone_cutter" #切石机
  repeat:
    - "oak"
    - "spruce"
    - "acacia"
    - "dark_oak"
    - "birch"
    - "jungle"
  input: "minecraft:{#type}_log" #输入物品ID
  output: "minecraft:{#type}_planks*8"

```

