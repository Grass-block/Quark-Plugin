package org.atcraftmc.quark.web;

import io.vertx.ext.jwt.JWT;
import org.atcraftmc.quark.web.http.HttpHandlerContext;
import org.atcraftmc.quark.web.http.HttpRequest;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.atcraftmc.qlib.command.QuarkCommand;

import java.util.List;
import java.util.Objects;

@QuarkCommand(name = "password")
public final class PasswordAuthorcation extends CoreCommand {
    public static final List<String> MODE_TAB_LIST = List.of("set", "reset");

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "set" -> SharedObjects.SHARED_THREAD_POOL.submit(() -> {
                PlayerAuthService.set(sender.getName(), args[1]);
                //Quark.LANGUAGE.sendMessage(sender, "auth", "password_set", args[1]);
            });
            case "reset" -> SharedObjects.SHARED_THREAD_POOL.submit(() -> {
                String rand = PlayerAuthService.generateRandom();
                PlayerAuthService.set(sender.getName(), rand);
                //Quark.LANGUAGE.sendMessage(sender, "auth", "password_set", rand);
            });
            default -> this.sendExceptionMessage(sender);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        switch (buffer.length) {
            case 1 -> tabList.addAll(MODE_TAB_LIST);
            case 2 -> {
                if (Objects.equals(buffer[0], "set")) tabList.add("<password>");
            }
        }
    }


    @HttpRequest("/auth/password")
    public void onAuthRequest(HttpHandlerContext context) {


        context.createJsonReturn().addProperty("token","jwt");
    }
}
