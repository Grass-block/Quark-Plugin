package org.tbstcraft.quark;

import me.gb2022.commons.TriState;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.PlayerDataService;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.tbstcraft.quark.foundation.TextSender;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.service.ServiceManager;
import org.tbstcraft.quark.internal.ProductService;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.placeholder.PlaceHolders;

import java.util.Properties;

@SuppressWarnings("TrailingWhitespacesInTextBlock")
public interface ProductInfo {
    Properties METADATA = new Properties();


    static String version() {
        return Quark.getInstance().getDescription().getVersion();
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

    static String logo(JavaPlugin p) {
        return ChatColor.translateAlternateColorCodes('&', """
                &d ______   __  __   ______   ______   __  __
                &d/\\  __ \\ /\\ \\/\\ \\ /\\  __ \\ /\\  == \\ /\\ \\/ /
                &d\\ \\ \\/\\_\\\\ \\ \\_\\ \\\\ \\  __ \\\\ \\  __< \\ \\  _"-.
                &d \\ \\___\\_\\\\ \\_____\\\\ \\_\\ \\_\\\\ \\_\\ \\_\\\\ \\_\\ \\_\\
                &d  \\/___/_/ \\/_____/ \\/_/\\/_/ \\/_/ /_/ \\/_/\\/_/     &fv%s
                &e ------------------------------------------------------------
                &7 Artifact by &fGrassBlock2022, &7Copyright &f[C]TBSTMC 2024.        
                """.formatted(p.getDescription().getVersion()));
    }

    static void sendStatsDisplay(CommandSender sender) {
        String dom = """
                {#yellow}一一一一一一一一一一一一一一一一一一一一一一一一一一一
                Statistics:
                  &7Version: &f%s
                  &7BuildTime: &f%s
                  &7Modules: &b%d&7/&f%d {click(command,/quark module list);color(gold)}[view]{;}
                  &7Packages: &b%d&7/&f%d {click(command,/quark package list);color(gold)}[view]{;}
                  &7Services: &b%d&7
                  &7PlayerData: &f%d
                  &7ModuleData: &f%d
                  &7CoreUA: &f%s 
                  &7InstanceID: {click(copy,%s);color(gold)}[copy]{;}
                  &7ProductID: {click(copy,%s);color(gold)}[copy]{;}
                {#yellow}一一一一一一一一一一一一一一一一一一一一一一一一一一一
                """;

        var text = ChatColor.translateAlternateColorCodes('&', dom.formatted(
                version() + "/api_" + apiMajorVersion() + "." + apiMinorVersion(),
                ProductInfo.METADATA.getProperty("build-time"),
                ModuleManager.getInstance().getIdsByStatus(TriState.FALSE).size(),
                ModuleManager.getInstance().getKnownModuleMetas().size(),
                PackageManager.getIdsByStatus(TriState.FALSE).size(),
                PackageManager.getAllPackages().size(),
                ServiceManager.all().size(),
                PlayerDataService.entryCount(),
                ModuleDataService.getEntryCount(),
                Quark.CORE_UA,
                Quark.getInstance().getInstanceUUID(),
                ProductService.getSystemIdentifier()
                                                                            ));
        TextSender.sendBlock(sender, TextBuilder.build(PlaceHolderService.format(text, PlaceHolders.quarkStats())));
    }

    static void sendInfoDisplay(CommandSender sender) {
        String s = """
                {logo}
                                
                 强大的Minecraft服务器综合管理插件。

                 官方网站: {color(aqua);underline;click(link,https://quark.tbstmc.xyz)}https://quark.tbstmc.xyz{none}
                 联系我们: {#aqua}tbstmc@163.com{none}
                                
                 测试服务器: ATCraft Network
                 开发: GrassBlock2022

                 {#red}[{#white}特别感谢{#red}]{#white}
                  - Mipa: Folia平台测试
                  - Modrinth: 提供更新服务
                  - OpenAI: ChatGPT用于多语言翻译和API解读             
                  
                 {#red}[{#white}第三方库{#red}]{#white}
                  - LevelDB: 数据存储
                  - AdventureAPI: 玩家界面实现
                                
                 {#white}Copyright @ATCraft Network(TBSTMC)(China). All Right Reserved.
                {#yellow}一一一一一一一一一一一一一一一一一一一一一一一一一一一
                """;
        if (ProductService.isActivated()) {
            s = s.replace("{activate}", "已激活");
        } else {
            s = s.replace("{activate}", "未激活");
        }

        if (!(sender instanceof ConsoleCommandSender)) {
            String prefix = "{#yellow}一一一一一一一一一一一一一一一一一一一一一一一一一一一\n";

            TextSender.sendBlock(sender, TextBuilder.build(prefix + s.replace("{logo}", textLogo())));
        } else {
            TextSender.sendBlock(sender, TextBuilder.build(s.replace("{logo}", logo(Quark.getInstance()))));
        }
    }
}
