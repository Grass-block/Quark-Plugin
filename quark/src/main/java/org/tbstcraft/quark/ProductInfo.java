package org.tbstcraft.quark;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.tbstcraft.quark.framework.text.TextBuilder;
import org.tbstcraft.quark.framework.text.TextSender;
import org.tbstcraft.quark.service.framework.PackageManager;
import org.tbstcraft.quark.service.framework.ProductService;

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
        //todo 完整性验证
        String s = """
                {color(yellow)} ─────────────────────────────────
                {logo} {#red}[{#white}{activate}{#red}] {#red}[{#white}已验证{#red}]
                                
                 {#gray}强大的Minecraft服务器综合管理插件。
                 {#gray}由 {color(white);click(link,http://grassblock2022.xyz)}GrassBlock2022{none}{#gray} 开发.

                   {color(gray)}官方网站: {color(aqua);underline;click(link,https://quark.tbstmc.xyz)}https://quark.tbstmc.xyz{none}
                   {color(gray)}联系我们: {color(aqua)}tbstmc@163.com {color(gold);click(copy,tbstmc@163.com)}[Copy]{none}
                   
                   {#white}Copyright @ATCraft Network(TBSTMC) 2024. All Rights Reserved.
                {color(yellow)} ─────────────────────────────────
                """;
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

    static void sendStatsDisplay(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        for (String s : PackageManager.getSubPacksFromServer()) {
            sb.append("{}   ").append("- ").append(s).append("\n");
        }
        String f = sb.toString();
        f = f.substring(0, f.length() - 1);

        TextSender.sendBlock(sender, TextBuilder.build("""
                {color(yellow)} ─────────────────────────────────
                统计信息:
                  {#white}已安装的模块: {#aqua}{#module_installed}
                  {#white}已启用的模块: {#aqua}{#module_enabled}
                  {#white}生成的玩家档案: {#aqua}{#player_data_count}
                  {#white}生成的模块档案: {#aqua}{#module_data_count}
                  {#white}已安装的子包:
                %s
                                
                  {#white}核心版本: {#aqua}{#quark_version}
                  {#white}框架版本: {#aqua}{#quark_framework_version}
                  {#white}文本引擎版本: {#aqua}{#quark_text_engine_version}
                  {#white}构建时间: {#aqua}{#build_time}
                {color(yellow)} ─────────────────────────────────
                """.formatted(f)));
    }
}
