package org.atcraftmc.quark.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.command.CommandSender;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.TaskService;

@QuarkCommand(name = "hitokoto")
@SLModule(version = "1.0.0")
public final class Hitokoto extends CommandModule {
    public static final HttpRequest FETCH = HttpRequest.https(HttpMethod.GET, "v1.hitokoto.cn")
            .browserBehavior(false)
            .build();

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        TaskService.async().run(() -> {
            JsonObject json = JsonParser.parseString(FETCH.request()).getAsJsonObject();

            MessageAccessor.send(this.getLanguage(), sender,
                                 "sentence",
                                 json.get("hitokoto").getAsString(),
                                 json.get("from").getAsString(),
                                 json.get("id").getAsString()
            );
        });
    }
}
