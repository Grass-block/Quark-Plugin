package org.tbstcraft.quark.internal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.text.TextBuilder;
import org.tbstcraft.quark.framework.text.TextSender;

import java.util.List;
import java.util.Objects;

@QuarkModule(version = "1.0.0", internal = true, available = FeatureAvailability.DEMO_ONLY)
@CommandRegistry(Advertisements.ShowAdCommand.class)
public final class Advertisements extends PackageModule {

    private interface Texts {
        String FRAME = """
                {#yellow}----------------------------------------
                 广告插播:
                                
                 %s
                                
                 {#gray}(广告由quark-demo框架提供，获取完整授权可不再显示。)
                 {#gray}(我们保证以上内容提供商的服务质量。)
                {#yellow}----------------------------------------
                """;

        AdvertisementInfo DUMMY = AdvertisementInfo.fromText("""
                 广告位招租 :D
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
    public static final class ShowAdCommand extends ModuleCommand<Advertisements> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            TextSender.sendBlock(sender, TextBuilder.build(Texts.DUMMY.build()));
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            super.onCommandTab(sender, buffer, tabList);
        }
    }
}
