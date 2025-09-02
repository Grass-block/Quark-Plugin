package org.atcraftmc.quark.web_auth;

import io.vertx.core.http.HttpServerRequest;
import me.gb2022.commons.TriState;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.quark.web.VertxHttpService;
import org.atcraftmc.quark.web.VertxRouter;
import org.bukkit.Bukkit;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.internal.PlayerIdentificationService;
import org.atcraftmc.starlight.core.TaskService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@SLModule
@CommandProvider(MinecraftSsoAuthorization.AnswerSsoLogin.class)
public class MinecraftSsoAuthorization extends PackageModule implements PluginCommandExecutor {
    private final Map<String, BlockingQueue<TriState>> waitingSessions = new HashMap<>();

    @VertxRouter("/api/auth/sso")
    public void ssoLogin(HttpServerRequest request) {
        request.response().putHeader("content-type", "text/plain");
        request.response().setStatusCode(200);

        var name = request.getParam("player");
        var safetyCode = request.getParam("safety-code");
        var playerId = PlayerIdentificationService.transformID(name);

        var player = Bukkit.getOfflinePlayer(name);
        if (!player.isOnline()) {
            request.response().end(VertxHttpService.json((j) -> {
                j.addProperty("success", false);
                j.addProperty("message", "player is not online");
            }));

            return;
        }

        var future = new ArrayBlockingQueue<TriState>(1);
        this.waitingSessions.put(playerId, future);

        TaskService.async().delay(1200, () -> future.add(TriState.UNKNOWN));//configured time

        try {
            switch (future.take()) {
                case TRUE -> request.response().end(VertxHttpService.json((j) -> {
                    j.addProperty("success", true);
                    j.addProperty("token", JWTService.token((p, o) -> o.put("op", player.isOp()), player.getUniqueId()));
                }));
                case FALSE -> request.response().end(VertxHttpService.json((j) -> {
                    j.addProperty("success", false);
                    j.addProperty("message", "rejected by mc client");
                }));
                case UNKNOWN -> request.response().end(VertxHttpService.json((j) -> {
                    j.addProperty("success", false);
                    j.addProperty("message", "verification timeout");
                }));
            }
        } catch (InterruptedException e) {
            request.response().end(VertxHttpService.json((j) -> {
                j.addProperty("success", false);
                j.addProperty("message", "[internal] exception: " + e.getMessage());
            }));
        }


        //wait for player confirm
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "true", "false");
    }

    @Override
    public void execute(CommandExecution context) {
        var playerID = PlayerIdentificationService.transformPlayer(context.requireSenderAsPlayer());
        var accepted = Boolean.parseBoolean(context.requireEnum(0, "true", "false"));

        var session = this.waitingSessions.get(playerID);

        if (session == null) {
            return;
        }

        session.add(accepted ? TriState.TRUE : TriState.FALSE);
    }

    @QuarkCommand(name = "answer-sso-login")
    public static final class AnswerSsoLogin extends ModuleCommand<MinecraftSsoAuthorization> {
        @Override
        public void init(MinecraftSsoAuthorization module) {
            setExecutor(module);
        }
    }
}
