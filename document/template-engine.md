# 模板引擎

文: GrassBlock2022

-----

### 文本格式化
提供基本的文本格式化符号，例如颜色，样式，换行等

**适用范围**
 - 开启聊天组件模块时的聊天栏
 - 语言文件
 - 全局变量
 - 配置UI

**模板**

```
{#id}
```

| 参数                    | 描述   | 示例值                         |
|-----------------------|------|-----------------------------|
| black                 | 黑色   | `§0`                        |
| dark_blue             | 深蓝色  | `§1`                        |
| dark_green            | 深绿色  | `§2`                        |
| dark_aqua             | 深青色  | `§3`                        |
| dark_red              | 深红色  | `§4`                        |
| dark_purple           | 深紫色  | `§5`                        |
| gold                  | 金色   | `§6`                        |
| gray                  | 灰色   | `§7`                        |
| dark_gray             | 深灰色  | `§8`                        |
| blue                  | 蓝色   | `§9`                        |
| green                 | 亮绿色  | `§a`                        |
| aqua                  | 亮蓝色  | `§b`                        |
| red                   | 红色   | `§c`                        |
| pink                  | 粉色   | `§d`                        |
| yellow                | 黄色   | `§e`                        |
| white                 | 白色   | `§f`                        |
| bold                  | 粗体   | `§l`                        |
| delete                | 删除线  | `§m`                        |
| underline             | 下划线  | `§n`                        |
| italic                | 斜体   | `§o`                        |
| reset                 | 重置格式 | `§r`                        |
| return                | 换行   | `\n`                        |
| 其他global_vars.yml定义的值 | --   | `server-name` = `TBSTCraft` |

### UI构建
提供UI-文字分离的动态语言引用,
访问id为本模块的语言条目。

**适用范围**
- 配置UI

**模板**
```
{msg#id}
```

(随机文字)
```
{rand#id}
```

### 本地语言引用
引用`quark-core`语言文件中`global_vars`栏的本地化引用

**适用范围**
- 语言文件

**模板**
```
{global#id}
```

**预设值**
```yaml
    sys_info: "[{#aqua}{#bold}系统{#white}]{#gray}"
    sys_warning: "[{#yellow}{#bold}系统{#white}]{#gray}"
    sys_error: "[{#red}{#bold}系统{#white}]{#gray}"
    sys_success: "[{#green}{#bold}系统{#white}]{#gray}"
    sys_operation: "[{#light_purple}{#bold}系统{#white}]{#gray}"
    hint: "[{#dark_aqua}{#bold}提示{#white}]{#gray}"
    warn: "[{#yellow}{#bold}警告{#white}]{#gray}"
    error: "[{#error}{#bold}错误{#white}]{#gray}"
```
