/**
 * @Date: 2024-09-28
 * @LastEditTime: 2024-10-13
 * @FilePath: \Vitepress\wiki\.vitepress\config.mts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 *
 * @Description https://vitepress.dev/reference/site-config
 * @Description https://vitepress.dev/reference/default-theme-config
 */
import {DefaultTheme, defineConfig} from 'vitepress'

const logo: string = 'https://raw.githubusercontent.com/Grass-block/Starlight/refs/heads/main/starlight-logo.png';

// @ts-ignore
export default defineConfig({
    title: "Starlight -「星辰」",
    description: "ATCraftMC自主研发的新一代综合管理插件",
    lang: "zh-CN",
    lastUpdated: true,
    head: [['link', {rel: 'icon', href: logo}]],

    "css": {
        preprocessorOptions: {
            css: {
                additionalData: `@import "style.css";`
            }
        }
    },

    themeConfig: {
        logo: logo,
        docFooter: {prev: '上一页', next: '下一页'},
        outline: {label: '📚目录', level: 'deep'},
        socialLinks: [{icon: 'github', link: 'https://github.com/ATCraftMC'}],
        search: {provider: 'local'},

        //below functions
        notFound: notFound(),
        nav: nav(),
        footer: footer(),
        sidebar: sidebar(),
    },
})

function notFound(): DefaultTheme.NotFoundOptions {
    return {
        title: '未找到页面',
        code: '404',
        quote: '无法找到您请求的页面或资源。',
        linkText: '返回首页',
    }
}

function nav(): DefaultTheme.NavItem[] {
    return [
        {text: '使用说明', link: '/content'},
        {text: '开发指南', link: '/dev'},
        {text: 'ATCraftMC️', link: 'https://atcraftmc.cn'},
        {text: '赞助', link: 'https://ifdian.net/a/TBSTmc'},
    ]
}

function footer(): DefaultTheme.Footer {
    return {
        message: '本Wiki根据 <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh-hans">CC BY-NC-SA 4.0 协议</a>发布',
        copyright: 'Copyright © 2021-present <a href="https://atcraftmc.cn">ATCraftMC 2025</a>'
    }
}

function sidebar(): DefaultTheme.Sidebar {
    return {
        '/desc': [
            {
                text: '概览', items: [
                    {text: '介绍', link: '/desc/index.md'},
                    {text: '配置文件', link: '/desc/config.md'},
                    {text: '文本组件', link: '/desc/text-component.md'},
                    {text: '用户协议', link: '/desc/eula.md'},
                ]
            },
            {
                text: '二次开发', items: [
                    {text: '数据包ID表', link: '/desc/dev/pack-scheme-id'},
                    {text: '制作语言包', link: '/desc/dev/make-language-pack'},
                    {text: '制作扩展包', link: '/desc/dev/make-extension-pack'},
                    {text: '调整插件平台', link: '/desc/dev/platform-injection'}
                ]
            }],

    }
}

function sidebar_starlight(): DefaultTheme.SidebarItem[] {
    return[


        {
            text: '基础内容 | starlight-base',
            collapsed: false,
            items: [
                {
                    text: '安全 | starlight-security',
                    items: [
                        //{text: '', link: '/starlight/starlight-base/starlight-security/'},
                        {text: '爆炸保护', link: '/starlight/starlight-base/starlight-security/explosion-defender'},
                        {text: 'IP检测防护', link: '/starlight/starlight-base/starlight-security/ip-defender'},
                        {text: '物品黑名单', link: '/starlight/starlight-base/starlight-security/item-defender'},
                        {text: '权限管理器', link: '/starlight/starlight-base/starlight-security/permission-manager'},
                    ]
                },
                {
                    text: '显示 | starlight-display', items: [
                        //{text: '', link: '/starlight/starlight-base/starlight-display/'},
                        {text: 'Boss栏公告', link: '/starlight/starlight-base/starlight-display/bossbar-announcement'},
                        {text: '聊天格式化', link: '/starlight/starlight-base/starlight-display/chat-format'},
                        {text: '自定义MOTD', link: '/starlight/starlight-base/starlight-display/custom-motd'},
                        {text: '自定义积分板', link: '/starlight/starlight-base/starlight-display/custom-scoreboard'},
                        {text: '自定义TAB', link: '/starlight/starlight-base/starlight-display/tab-menu'},
                        {text: 'WE选区渲染', link: '/starlight/starlight-base/starlight-display/we-session-renderer'},
                        {text: '悬浮文字', link: '/starlight/starlight-base/starlight-display/hover-display'},
                        {text: '挂机检测', link: '/starlight/starlight-base/starlight-display/afk'},
                    ]
                },
                {
                    text: '聊天 | starlight-chat',
                    items: [
                        //{text: '', link: '/starlight/starlight-base/starlight-chat/'},
                        {text: '聊天At', link: '/starlight/starlight-base/starlight-chat/chat-at'},
                        {text: '', link: '/starlight/starlight-base/starlight-chat/'}
                    ]
                },
                {
                    text: '管理 | starlight-management',
                    items: [
                        {
                            text: '高级Plugins命令',
                            link: '/starlight/starlight-base/starlight-management/advanced-plugin-command'
                        },
                    ]
                },
                {
                    text: '实用工具 | starlight-utilities',
                    items: []
                }
            ],
        },
        {
            text: '游戏修改 | starlight-game',
            collapsed: true,
            items: [
                {
                    text: '传送 | starlight-warps',
                    items: [
                        //{text: '', link: '/starlight/starlight-game/starlight-warps/'},
                        {text: '导航点', link: '/starlight/starlight-game/starlight-warps/waypoint'},
                        {text: '传送请求', link: '/starlight/starlight-game/starlight-warps/tpa'},
                        {text: '回到死亡点', link: '/starlight/starlight-game/starlight-warps/back-to-death'},
                    ]
                },
            ],
        },
        {
            text: '大厅功能 | starlight-lobby:*',
            collapsed: true,
            items: [],
        },
        {
            text: '集群交互 | starlight-proxy',
            collapsed: true,
            items: []
        },
        {
            text: '网络服务 | starlight-web',
            collapsed: true,
            items: [],
        }
    ]
}