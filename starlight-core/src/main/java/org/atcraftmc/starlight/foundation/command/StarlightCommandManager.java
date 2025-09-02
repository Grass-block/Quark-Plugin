package org.atcraftmc.starlight.foundation.command;

import org.atcraftmc.qlib.command.CommandManager;
import org.atcraftmc.qlib.command.execute.CommandErrorType;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.Starlight;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.atcraftmc.starlight.core.permission.PermissionService;

public final class StarlightCommandManager extends CommandManager {
    private final LanguageEntry messages;

    public StarlightCommandManager(Starlight handle) {
        super(handle);
        this.messages = handle.language().entry("starlight-core:command");
    }

    public static CommandManager getInstance() {
        return Starlight.instance().getCommandManager();
    }

    @Override
    public void sendExceptionMessage(CommandSender sender, Throwable... throwable) {
        this.messages.item("exception").send(sender);
    }

    @Override
    public void sendExecutionErrorMessage(CommandSender sender, CommandErrorType type, Object... objects) {
        var id = switch (type) {
            case MISSING_ARGUMENTS -> "error-missing";
            case ARGUMENT_TYPE_INT -> "error-argument-type-int";
            case ARGUMENT_TYPE_FLOAT -> "error-argument-type-float";
            case ARGUMENT_TYPE_DOUBLE -> "error-argument-type-double";
            case ARGUMENT_TYPE_ENUM -> "error-argument-type-enum";
            case REQUIRE_EXIST_PLAYER, REQUIRE_ONLINE_PLAYER -> "error-require-player";
            case ARGUMENT_NUMBER_OUT_BOUND -> "error-argument-bound";
            case REQUIRE_PERMISSION -> "error-lack-permission";
            case LACK_ANY_PERMISSION -> "error-lack-any-permission";
            case LACK_ALL_PERMISSION -> "error-lack-all-permission";
            case REQUIRE_SENDER_PLAYER -> "error-sender-is-not-player";
        };

        this.messages.item(id).send(sender, objects);
    }

    @Override
    public void sendPermissionMessage(CommandSender sender, String s) {
        this.messages.item("error-lack-permission").send(sender, "{;}" + s);
    }

    @Override
    public void sendPlayerOnlyMessage(CommandSender sender) {
        this.messages.item("player-only").send(sender);
    }

    @Override
    public void createPermission(String s) {
        PermissionService.createPermission(s);
    }

    @Override
    public Permission getPermission(String s) {
        return PermissionService.getPermission(s);
    }

    @Override
    public String getCommandNamespace() {
        return "quark";
    }
}
