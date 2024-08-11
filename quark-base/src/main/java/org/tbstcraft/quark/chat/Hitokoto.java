package org.tbstcraft.quark.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

@QuarkCommand(name = "hitokoto")
@QuarkModule(version = "1.0.0")
public final class Hitokoto extends CommandModule {
    public static final HttpRequest FETCH = HttpRequest.https(HttpMethod.GET, "v1.hitokoto.cn")
            .browserBehavior(false)
            .build();

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        TaskService.asyncTask(() -> {
            JsonObject json = JsonParser.parseString(FETCH.request()).getAsJsonObject();

            getLanguage().sendMessage(
                    sender,
                    "sentence",
                    json.get("hitokoto").getAsString(),
                    json.get("from").getAsString(),
                    json.get("id").getAsString()
            );
        });
    }
}
