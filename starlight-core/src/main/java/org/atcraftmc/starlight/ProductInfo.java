package org.atcraftmc.starlight;

import me.gb2022.commons.TriState;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.core.placeholder.PlaceHolders;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.framework.SLModuleManager;
import org.atcraftmc.starlight.framework.SLServiceManager;
import org.atcraftmc.starlight.framework.packages.SLPackageManager;
import org.atcraftmc.starlight.internal.ProductService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Properties;

@SuppressWarnings("TrailingWhitespacesInTextBlock")
public interface ProductInfo {
    Properties METADATA = new Properties();
    int API_VERSION = 50;
    int BSTATS_ID = 22683;
    String PLUGIN_ID = "starlight";
    String CORE_ID = "starlight-core";
    String CORE_UA = "starlight/sl-beta-0.80.1[cv10-higgsfield]";

    static String version() {
        return Starlight.instance().getDescription().getVersion();
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
        return "{color(purple)}Starlight {color(gray)} - {color(white)}v%s".formatted(version());
    }

    static String logo(JavaPlugin p) {
        return ChatColor.translateAlternateColorCodes('&', """
                &d一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一
                &d&l      ____ __              __ _        __   __\s
                &d&l    / __// /_ ___ _ ____ / /(_)___ _ / /  / /_
                &d&l   _\\ \\ / __// _ `// __// // // _ `// _ \\/ __/
                &d&l  /___/ \\__/ \\_,_//_/  /_//_/ \\_, //_//_/\\__/\s
                &d&l                         /___/                    -&fv%s
                
                &7 Artifact by &fGrassBlock2022, &7Copyright &f[C]ATCraftMC 2025.
                &d一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一
                """.formatted(p.getDescription().getVersion()));
    }

    static void sendStatsDisplay(CommandSender sender) {
        String dom = """
                {#yellow}一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一
                Statistics:
                  &7Version: &f%s
                  &7BuildTime: &f%s
                  &7Modules: &b%d&7/&f%d {click(command,/quark module list);color(gold)}[view]{;}
                  &7Packages: &b%d&7/&f%d {click(command,/quark package list);color(gold)}[view]{;}
                  &7Services: &b%d&7
                
                  &7CoreUA: &f%s 
                  &7InstanceID: {click(copy,%s);color(gold)}[copy]{;}
                  &7ProductID: {click(copy,%s);color(gold)}[copy]{;}
                {#yellow}一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一
                """;

        var text = ChatColor.translateAlternateColorCodes('&', dom.formatted(
                version() + "/api_" + apiMajorVersion() + "." + apiMinorVersion(),
                ProductInfo.METADATA.getProperty("build-time"),
                SLModuleManager.getInstance().getIdsByStatus(TriState.TRUE).size(),
                SLModuleManager.getInstance().getModules().size(),
                SLPackageManager.getIdsByStatus(TriState.TRUE).size(),
                SLPackageManager.getAllPackages().size(),
                SLServiceManager.all().size(),
                CORE_UA,
                Starlight.instance().getInstanceUUID(),
                ProductService.getSystemIdentifier()
        ));
        TextSender.sendBlock(sender, TextBuilder.build(PlaceHolderService.format(text, PlaceHolders.quarkStats())));
    }

    static void sendInfoDisplay(CommandSender sender) {
        String s = """
                {logo}
                
                 A plugin containing everything you need. :D
                
                 Website & Docs: {#aqua}https://starlight-plugin.pages.dev{#reset}
                 Official Release: {#aqua}https://modrinth.com/plugin/starlight-plugin{#reset}
                 Contact: {#aqua}tbstmc@163.com{#reset}
                
                {#purple}> Credits & Special thanks:
                {#white} - GrassBlock2022: {#gray}Core developer.
                {#white} - Mipa/IdealMC: {#gray}Production environment test.
                {#white} - Modrinth: {#gray}Publishing and version check service.
                {#white} - OpenAI/ChatGPT: {#gray}Multi-language translations.
                {#white} - {#gray}(and anyone who use this plugin and feedback)
                
                {#purple}> Third party libraries:
                {#white} - LevelDB-JNI: {#gray}Legacy Data storage.
                {#white} - AdventureAPI: {#gray}Player view implementation.
                {#white} - JavaX-Mail: {#gray}SMTP Service implementation.
                {#white} - Aho-Corasick: {#gray}Chat filtering algorithm.
                
                 {#white}Copyright @ATCraftMC(TBSTMC)(China). All Right Reserved.
                {#purple}一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一
                """;
        if (ProductService.isActivated()) {
            s = s.replace("{activate}", "已激活");
        } else {
            s = s.replace("{activate}", "未激活");
        }

        if (!(sender instanceof ConsoleCommandSender)) {
            String prefix = "{#purple}一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一一\n";

            TextSender.sendBlock(sender, TextBuilder.build(prefix + s.replace("{logo}", textLogo())));
        } else {
            for (var s2 : PluginPlatform.global().globalFormatMessage(s.replace("{logo}", logo(Starlight.instance()))).split("\n")) {
                sender.sendMessage(s2);
            }
        }
    }
}
