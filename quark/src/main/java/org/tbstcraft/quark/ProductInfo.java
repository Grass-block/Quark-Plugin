package org.tbstcraft.quark;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.service.base.ProductService;
import org.tbstcraft.quark.util.text.TextBuilder;
import org.tbstcraft.quark.util.text.TextSender;

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
                {color(yellow)}------------------------------------------------------------
                {color(gray)}Artifact by {color(white)}GrassBlock2022, {color(gray)}Copyright {color(white)}[C]TBSTMC 2024.
                """.formatted(version());
    }

    static void sendStatsDisplay(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        for (String s : PackageManager.getSubPacksFromServer()) {
            sb.append("{;}   ").append("- ").append(s).append("\n");
        }
        String f = sb.toString();
        f = f.substring(0, f.length() - 1);

        String s = """
                 ================{#red}[{#white}Quark-统计信息{#red}]{#white}================
                {;}
                  已安装的模块: {#aqua}{#module_installed}{#white}
                  已启用的模块: {#aqua}{#module_enabled}{#white}
                  生成的玩家档案: {#aqua}{#player_data_count}{#white}
                  生成的模块档案: {#aqua}{#module_data_count}{#white}
                  已安装的子包:
                %s
                  核心版本: {#aqua}{#quark_version}{#white}
                  框架版本: {#aqua}{#quark_framework_version}{#white}
                  构建时间: {#aqua}{#build_time}{#white}
                """.formatted(f);
        TextSender.sendBlock(sender, TextBuilder.build(s));
    }

    static void sendInfoDisplay(CommandSender sender) {
        String s = """
                {logo}
                
                 强大的Minecraft服务器综合管理插件。

                 官方网站: {color(aqua);underline;click(link,https://quark.tbstmc.xyz)}https://quark.tbstmc.xyz{none}
                 联系我们: {#aqua}tbstmc@163.com{none}
                
                 测试服务器: ATCraft Network
                 开发: GrassBlock2022
                 多语言: ChatGPT by OpenAI
                 特别感谢: Mipa[Folia平台测试] 以及所有的第三方库开发者
                
                 {#red}[{#white}第三方库{#red}]{#white}
                  - LevelDB: 数据存储
                  - AdventureAPI: 玩家界面实现
                
                 {#white}Copyright @ATCraft Network(TBSTMC)(China). All Right Reserved.
                """;
        if (ProductService.isActivated()) {
            s = s.replace("{activate}", "已激活");
        } else {
            s = s.replace("{activate}", "未激活");
        }

        if (!(sender instanceof ConsoleCommandSender)) {
            String prefix = " ================{#red}[{#white}Quark-关于{#red}]{#white}================\n";

            TextSender.sendBlock(sender, TextBuilder.build(prefix + s.replace("{logo}", textLogo())));
        } else {
            TextSender.sendBlock(sender, TextBuilder.build(s.replace("{logo}", logo())));
        }
    }
}
