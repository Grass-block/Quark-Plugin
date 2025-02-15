# 游戏
文：GrassBlock2022

-----
为服务器带来一些游戏性上的修改，例如椅子等

### StairSeat | 楼梯椅子

允许玩家在楼梯上坐下。
**模块id:**

```
quark_game:stair_seat
```

**说明**

- 空手右键楼梯才能坐下
- 如果一位玩家占用了一个楼梯那么你是坐不下的
- 如果一个楼梯被破坏了, 你会被甩下来
- 倒着的楼梯不能坐(这曾经是个bug现在修复了)
- 不用担心楼梯朝向，系统会自动校正你的玩家位置


### MinecartController | 矿车控制器

允许玩家在动力铁轨上手动驾驶矿车加减速，并获得高速度。
**模块id:**

```
quark_game:minecart_controller
```

**说明**

- 进入矿车的时候，矿车是不能自主移动的，且您的物品栏将被移动到中央位置。
- 将物品栏向右滑动进入加速档位，矿车将向您面朝的方向自动运行。
- 物品栏向右滑动，越过中央槽位即开始加速。档位越大加速度越大，直到设定最大速度(默认30m/s)。
- 物品栏向左滑动，越过中央槽位即开始减速。档位越大减速度越大，直到停车。

**配置文件**

```yaml
  minecart_controller:
    #显示在玩家快捷栏的仪表板UI
    ui: '{run_mode}{#white}({msg#level}{level}) {#yellow}|{#white} {msg#speed}{speed} {#yellow}|{#white} {msg#acceleration}{acceleration}m/s^2'
    max_speed: 1.5 #最大速度(方块/游戏刻)
    thrust_1_acceleration: 0.08 #各个档位的加减速度(方块/游戏刻^2)
    thrust_2_acceleration: 0.16
    thrust_3_acceleration: 0.27
    thrust_4_acceleration: 0.32
    thrust_-1_acceleration: -0.08
    thrust_-2_acceleration: -0.29
    thrust_-3_acceleration: -0.32
    thrust_-4_acceleration: -0.5
```