package org.tbstcraft.quark.foundation.command;

import org.atcraftmc.qlib.command.CommandManager;
import org.atcraftmc.qlib.command.execute.CommandErrorType;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.BundledPackageLoader;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.internal.permission.PermissionService;

public final class QuarkCommandManager extends CommandManager {
    private final LanguageEntry messages = Quark.LANGUAGE.entry("command");

    public QuarkCommandManager(Plugin handle) {
        super(handle);
    }

    public static CommandManager getInstance() {
        return Quark.getInstance().getCommandManager();
    }

    @Override
    public void sendExceptionMessage(CommandSender sender, Throwable... throwable) {
        this.messages.sendMessage(sender, "exception");
    }

    @Override
    public void sendExecutionErrorMessage(CommandSender sender, CommandErrorType type, Object... objects) {
        String id = switch (type) {
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

        this.messages.sendMessage(sender, id, objects);
    }

    @Override
    public void sendPermissionMessage(CommandSender sender, String s) {
        this.messages.sendMessage(sender, "error-lack-permission", "{;}" + s);
    }

    @Override
    public void sendPlayerOnlyMessage(CommandSender sender) {
        this.messages.sendMessage(sender, "player-only");
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
