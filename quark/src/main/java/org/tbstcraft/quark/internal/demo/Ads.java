package org.tbstcraft.quark.internal.demo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.framework.command.CommandRegistry;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.text.TextBuilder;
import org.tbstcraft.quark.framework.text.TextSender;

import java.util.List;
import java.util.Objects;

@QuarkModule(version = "1.0.0")
@CommandRegistry(Ads.ShowAdCommand.class)
public final class Ads extends PackageModule {


    private interface Texts {
        String FRAME = """
                {#yellow}----------------------------------------
                 广告插播:
                
                 %s
                
                 {#gray}(广告由quark-demo框架提供，获取完整授权可不再显示。)
                 {#gray}(我们保证以上内容提供商的服务质量。)
                {#yellow}----------------------------------------
                """;

        AdvertisementInfo ARTISTIC_CRAFT = AdvertisementInfo.fromText("""
                 {#aqua}ArtisticCraft{#gray} - {#gold}Rage your dream
                 
                  {#white}体验最纯净的高版本原版生存，在一方静谧之中享受游戏人生。
                  {#white}(原版生存生存类群组服务器 群号:{#aqua}{#underline}866097840{#white})
                """);

        AdvertisementInfo TBSTCRAFT=AdvertisementInfo.fromText("""
                 {#aqua}TBSTCraft 2024{#gray} - {#gold}不忘初心，砥砺前行
                 
                  {#white}高版本原版/模组多元创造服务器，来看看最新版本的Quark都有什么特性吧
                  {#white}群号:{#aqua}{#underline}733345889{#white} 官网:{#aqua}{#underline}https://tbstmc.xyz{#white}
                """);
    }

    private static final class AdvertisementInfo {
        public static final String CONTENT_TYPE_MC = "mc_server";

        private final String content;
        private final String contentType;

        private AdvertisementInfo(String content, String contentType) {
            this.content = content;
            this.contentType = contentType;
        }

        private AdvertisementInfo(String jsonText) {
            JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
            this.content = json.get("content").getAsString();
            this.contentType = json.get("content_type").getAsString();
        }

        static AdvertisementInfo fromText(String content, String contentType) {
            return new AdvertisementInfo(content, contentType);
        }

        static AdvertisementInfo fromText(String content) {
            return fromText(content, CONTENT_TYPE_MC);
        }

        static AdvertisementInfo fromJson(String jsonText) {
            JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
            String content = json.get("content").getAsString();
            String contentType = json.get("content_type").getAsString();

            return fromText(content, contentType);
        }

        public boolean isMinecraftServer() {
            return Objects.equals(this.contentType, CONTENT_TYPE_MC);
        }

        public String build() {
            return Texts.FRAME.formatted(this.content);
        }
    }

    @QuarkCommand(name = "show-ads", op = true)
    public static final class ShowAdCommand extends ModuleCommand<Ads> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            TextSender.sendBlock(sender, TextBuilder.build(Texts.TBSTCRAFT.build()));
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            super.onCommandTab(sender, buffer, tabList);
        }
    }
}
