package org.atcraftmc.starlight.utilities;

import com.google.gson.JsonParser;
import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.command.CommandSender;

@QuarkCommand(name = "hitokoto")
@ApplicationModule(id = "hitokoto", version = "1.0.0")
public final class Hitokoto extends SLCommandModule {
    public static final HttpRequest FETCH = HttpRequest.https(HttpMethod.GET, "v1.hitokoto.cn")
            .browserBehavior(false)
            .build();

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        TaskService.async().run(() -> {
            var json = JsonParser.parseString(FETCH.request()).getAsJsonObject();

            MessageAccessor.send(this.language(), sender,
                                 "sentence",
                                 json.get("hitokoto").getAsString(),
                                 json.get("creator").getAsString(),
                                 json.get("from").getAsString()
            );
        });
    }
}
