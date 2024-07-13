package org.tbstcraft.quark.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.util.NetworkUtil;

import java.io.IOException;

@QuarkCommand(name = "hitokoto")
@QuarkModule(version = "1.0.0")
public final class Hitokoto extends CommandModule {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            String dom = NetworkUtil.httpGet("https://v1.hitokoto.cn/", false);
            JsonObject json = JsonParser.parseString(dom).getAsJsonObject();

            getLanguage().sendMessage(sender, "sentence",
                    json.get("hitokoto").getAsString(),
                    json.get("from").getAsString(),
                    json.get("id").getAsString()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
