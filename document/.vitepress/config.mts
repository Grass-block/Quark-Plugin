// noinspection JSUnusedGlobalSymbols

/**
 * @Date: 2024-09-28
 * @LastEditTime: 2024-10-13
 * @FilePath: \Vitepress\wiki\.vitepress\config.mts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 *
 * @Description https://vitepress.dev/reference/site-config
 * @Description https://vitepress.dev/reference/default-theme-config
 */
// @ts-ignore
function sidebar_quark(): DefaultTheme.SidebarItem[] {
    return [
        {
            text: '概览', items: [
                {text: '介绍', link: '/quark/index'},
                {text: '聊天组件', link: '/quark/chat-components'},
                {text: '扩展包索引', link: '/quark/package-index'},
                {text: '配置文件', link: '/quark/configuration'},
                {text: '模板引擎', link: '/quark/template-engine'},
            ]
        },
        {
            text: '二次开发', items: [
                {text: '数据包ID表', link: '/quark/dev/pack-scheme-id'},
                {text: '制作语言包', link: '/quark/dev/make-language-pack'},
                {text: '制作扩展包', link: '/quark/dev/make-extension-pack'}
            ]
        },
        {
            text: '基础内容 | quark-base',
            collapsed: false,
            items: [
                {
                    text: '安全 | quark-security',
                    items: [
                        //{text: '', link: '/quark/quark-base/quark-security/'},
                        {text: '爆炸保护', link: '/quark/quark-base/quark-security/explosion-defender'},
                        {text: 'IP检测防护', link: '/quark/quark-base/quark-security/ip-defender'},
                        {text: '物品黑名单', link: '/quark/quark-base/quark-security/item-defender'},
                        {text: '权限管理器', link: '/quark/quark-base/quark-security/permission-manager'},
                    ]
                },
                {
                    text: '显示 | quark-display', items: [
                        //{text: '', link: '/quark/quark-base/quark-display/'},
                        {text: 'Boss栏公告', link: '/quark/quark-base/quark-display/bossbar-announcement'},
                        {text: '聊天格式化', link: '/quark/quark-base/quark-display/chat-format'},
                        {text: '自定义MOTD', link: '/quark/quark-base/quark-display/custom-motd'},
                        {text: '自定义积分板', link: '/quark/quark-base/quark-display/custom-scoreboard'},
                        {text: '自定义TAB', link: '/quark/quark-base/quark-display/tab-menu'},
                        {text: 'WE选区渲染', link: '/quark/quark-base/quark-display/we-session-renderer'},
                        {text: '悬浮文字', link: '/quark/quark-base/quark-display/hover-display'},
                        {text: '挂机检测', link: '/quark/quark-base/quark-display/afk'},
                    ]
                },
                {
                    text: '聊天 | quark-chat',
                    items: [
                        //{text: '', link: '/quark/quark-base/quark-chat/'},
                        {text: '聊天At', link: '/quark/quark-base/quark-chat/chat-at'},
                        {text: '', link: '/quark/quark-base/quark-chat/'}
                    ]
                },
                {
                    text: '管理 | quark-management',
                    items: [
                        {
                            text: '高级Plugins命令',
                            link: '/quark/quark-base/quark-management/advanced-plugin-command'
                        },
                    ]
                },
                {
                    text: '实用工具 | quark-utilities',
                    items: []
                }
            ],
        },
        {
            text: '游戏修改 | quark-game',
            collapsed: true,
            items: [
                {
                    text: '传送 | quark-warps',
                    items: [
                        //{text: '', link: '/quark/quark-game/quark-warps/'},
                        {text: '导航点', link: '/quark/quark-game/quark-warps/waypoint'},
                        {text: '传送请求', link: '/quark/quark-game/quark-warps/tpa'},
                        {text: '回到死亡点', link: '/quark/quark-game/quark-warps/back-to-death'},
                    ]
                },
            ],
        },
        {
            text: '大厅功能 | quark-lobby:*',
            collapsed: true,
            items: [],
        },
        {
            text: '集群交互 | quark-proxy',
            collapsed: true,
            items: []
        },
        {
            text: '网络服务 | quark-web',
            collapsed: true,
            items: [],
        }
    ]
}