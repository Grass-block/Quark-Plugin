---
# https://vitepress.dev/reference/default-theme-home-page
layout: home
hero:
  name: "Starlight"
  text: "&nbsp;&nbsp;&nbsp;&nbsp;—「星辰」"
  tagline: "ATCraftMC自主研发的新一代综合管理插件"
  alt: ATC
  actions:
      - theme: brand
        text: 快速开始
        link: /content
      - theme: alt
        text: 扩展开发
        link: /dev
  image:
    src: https://raw.githubusercontent.com/Grass-block/Starlight/refs/heads/main/starlight-logo.png
    alt: VitePress
features:
  - title: 高度模块化
    icon: <img src="http://localhost:5173/logo2.png" />
    details:
      模块化功能拆分细致，且每个模块均可单独启停。
      各个模块之间完全独立，核心正常加载则整个插件将不被任何模块/子包影响。
  - title: 易于维护
    icon: 🛠️
    details:
      配置文件一键自动刷新，支持开箱即用，无需复杂的准备配置工作。
      不会注入服务器后端/原版数据，卸载无任何影响。
  - title: 性能优越
    icon: 🧷
    details:
      静止状态下实测插件占用服务器计算时间<1.5ms，启动时间小于650ms(仅供参考)
      性能远高于同类功能的多插件替代实现。
---