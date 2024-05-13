package org.tbstcraft.quark;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.tbstcraft.quark.framework.text.TextBuilder;
import org.tbstcraft.quark.framework.text.TextSender;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.service.base.ProductService;

import java.util.Properties;

public interface ProductInfo {
    Properties METADATA = new Properties();


    static String version() {
        return Quark.PLUGIN.getDescription().getVersion();
    }


    static int archVersion() {
        return Integer.parseInt(String.valueOf(version().charAt(0)));
    }

    static int apiMajorVersion() {
        return Integer.parseInt(String.valueOf(version().charAt(2)));
    }

    static int apiMinorVersion() {
        return Integer.parseInt(String.valueOf(version().charAt(3)));
    }

    static int minorVersion() {
        return Integer.parseInt(String.valueOf(version().charAt(4)));
    }


    static int apiVersion() {
        return apiMajorVersion() * 10 + apiMinorVersion();
    }


    static String textLogo() {
        return "{color(purple)}Quark {color(gray)} - {color(white)}v%s".formatted(version());
    }

    static String logo() {
        return """
                {color(purple)} ______   __  __   ______   ______   __  __
                {color(purple)}/\\  __ \\ /\\ \\/\\ \\ /\\  __ \\ /\\  == \\ /\\ \\/ /
                {color(purple)}\\ \\ \\/\\_\\\\ \\ \\_\\ \\\\ \\  __ \\\\ \\  __< \\ \\  _"-.
                {color(purple)} \\ \\___\\_\\\\ \\_____\\\\ \\_\\ \\_\\\\ \\_\\ \\_\\\\ \\_\\ \\_\\
                {color(purple)}  \\/___/_/ \\/_____/ \\/_/\\/_/ \\/_/ /_/ \\/_/\\/_/       {color(white)}v%s
                """.formatted(version());
    }

    static void sendInfoDisplay(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        for (String s : PackageManager.getSubPacksFromServer()) {
            sb.append("{}   ").append("- ").append(s).append("\n");
        }
        String f = sb.toString();
        f = f.substring(0, f.length() - 1);

        String s = """
                {#yellow}==========[{#purple}Quark{#white}-{#aqua}关于{#yellow}]==========
                
                {logo} {#red}[{#white}{activate}{#red}]
 
                 强大的Minecraft服务器综合管理插件。

                 官方网站: {color(aqua);underline;click(link,https://quark.tbstmc.xyz)}https://quark.tbstmc.xyz{none}
                 联系我们: {#aqua}tbstmc@163.com{none}
                   
                 {#red}[{#white}贡献名单{#red}]{#white}
                  - 测试服务器: ATCraft Network
                  - 开发: GrassBlock2022
                  - 多语言: ChatGPT by OpenAI
                   
                 {#red}[{#white}鸣谢名单{#red}]{#white}
                  - 土拨鼠: 测试支持, 创意提供
                  - OFT服务器: Spigot平台测试
                  - Mipa_: Folia平台测试
                  - 以及所有的第三方库开发者, Thanks!
                  
                 {#red}[{#white}第三方库{#red}]{#white}
                  - LevelDB: 数据存储
                  - AdventureAPI: 玩家界面实现
                  - NettyI/O: 网络系统支持
                  - AdvancePluginMessenger: 客户端通信/服务端组网
                  
                 {#red}[{#white}统计信息{#red}]{#white}
                  - 已安装的模块: {#aqua}{#module_installed}{#white}
                  - 已启用的模块: {#aqua}{#module_enabled}{#white}
                  - 生成的玩家档案: {#aqua}{#player_data_count}{#white}
                  - 生成的模块档案: {#aqua}{#module_data_count}{#white}
                  - 已安装的子包:
                %s
                  - 核心版本: {#aqua}{#quark_version}{#white}
                  - 框架版本: {#aqua}{#quark_framework_version}{#white}
                  - 构建时间: {#aqua}{#build_time}{#white}
                   
                 {#white}Copyright @ATCraft Network(TBSTMC) 2024. All Rights Reserved.
                   
                {#yellow}==========[{#purple}Quark{#white}-{#aqua}关于{#yellow}]==========
                """.formatted(f);
        if (ProductService.isActivated()) {
            s = s.replace("{activate}", "已激活");
        } else {
            s = s.replace("{activate}", "未激活");
        }

        if (!(sender instanceof ConsoleCommandSender)) {
            TextSender.sendBlock(sender, TextBuilder.build(s.replace("{logo}", textLogo())));
        } else {
            TextSender.sendBlock(sender, TextBuilder.build(s.replace("{logo}", textLogo())));
        }
    }
}
