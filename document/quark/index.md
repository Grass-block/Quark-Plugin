# Quark插件 <Badge>0.64.14-LatestBuild</Badge>

文: GrassBlock2022

-----

### 简介

Quark(中文名: 夸克)是我服自主研发的综合管理型插件，目前已应用于TBSTCraft服务器。
由于服务器公益化运营模式带来的长期支出可能导致腐竹无法负担，~~故我们将此插件以商业化形式对外出售~~。
目前为加快技术升级，我们将插件本体进行公开。
如果您觉得使用体验还不错的话，那不妨赞助给我们赞助一下。所得资金的部分将用于服务器的运营支出，部分将用于插件的研发成本回填。

### 特性

- 高性能: i7-12700平台实测paper1.18.2实测启动时间不足60ms。
- 模块化: 功能拆分细致，且每个模块均可单独启停。
- 健壮性: 各个模块之间完全独立，核心正常加载则整个插件将不被任何模块/子包影响。
- 简单: 配置文件一键自动刷新，支持开箱即用，无需复杂的准备配置工作。
- 低侵入: 不会注入服务器后端/原版数据，卸载无任何影响。

### 兼容性

#### Paper和上游服务端

从3.54.13开始，Quark的开发中心从Spigot/Bukkit移向Paper。
这将提供全新的功能，API和文本引擎。新的功能将仍然适配Paper上游平台，
但是文本引擎的文字互动功能将会无法使用。我们仍然建议您使用Paper及其下游服务端，
因为其拥有更完善的API和更好的性能。

#### 关于Folia适配

在Folia服务端上，Quark将会自动开启专用的FoliaTaskManager后端。
同时，您会收到一条关于Folia兼容性问题的警告。请不要反馈任何有关API兼容性的问题。
其利用Folia新增的Scheduler，可以优化Quark的性能。
由于Folia的API不完善，我们不建议您在FoliaAPI完善之前使用Quark。

#### 附: 兼容性和平台特性差异表

| Platform           | Features | Interactive Text | Support | Description   |
|--------------------|----------|------------------|---------|---------------|
| Paper              | Full     | Full             | Full    | Recommended   |
| Spigot             | Most     | Full             | BugFix  | --            |
| Bukkit             | Few      | No               | org.tbstcraft.quark.Test    | --            |
| Folia              | Most     | Full             | BugFix  | No hot reload |
| Mohist             | Few      | No               | org.tbstcraft.quark.Test    | No hot reload |
| Legacy(any<1.14.4) | Most     | Yes(>1.9)        | BugFix  | --            |

### 用户须知

#### 数据收集

我们可能会收集您的部分服务端和系统软硬件信息用于 BStats/PluginMetrics 的统计。

#### 文档搬运

您可以搬运Quark相关的文档到您的wiki, 但请注意:

- 我们不对您的搬运内容负责
- 请注明该内容为搬运并注明源站地址
- 请及时更新您的文档
- 您可以不搬运关于用户协议的内容

#### 您的义务

- 反馈任何您遇到的Bug
- 定时更新您的插件到最新版本

#### 您不能做的

- 随意分发插件本体
- 声明您是本插件的所有权人（你用我们的API二次开发的扩展包例外）
- 禁止以任何形式（包括但不限于售卖，赠送）等方式二次分发
- 随意分发源代码（修改过的也不行）
