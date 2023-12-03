package org.tbstcraft.quark.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.Quark;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractCommand extends org.bukkit.command.Command {
    protected AbstractCommand() {
        super("");
    }

    protected final void init() {
        this.setName(this.getName());
    }

    public boolean isOP() {
        return this.getClass().getAnnotation(QuarkCommand.class).op();
    }


    //abstraction
    public abstract void onCommand(CommandSender sender, String[] args);

    public abstract void onCommandTab(CommandSender sender, String[] args, List<String> tabList);


    //util
    public void sendExceptionMessage(CommandSender sender) {
        Quark.LANGUAGE.sendMessageTo(sender, "command", "exception");
    }

    public void sendPermissionMessage(CommandSender sender) {
        Quark.LANGUAGE.sendMessageTo(sender, "command", "lack_permission");
    }

    protected final void checkException(boolean predict) {
        if (predict) {
            return;
        }
        throw new RuntimeException("FORMAT_ERROR");
    }

    protected final boolean isBooleanOption(String arg) {
        return Objects.equals(arg, "true") || Objects.equals(arg, "false");
    }


    //adapter
    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> list = new ArrayList<>();
        if (this.isOP() && !sender.isOp()) {
            return list;
        }
        this.onCommandTab(sender, args, list);
        return list;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (this.isOP() && !sender.isOp()) {
            this.sendPermissionMessage(sender);
            return false;
        }
        try {
            this.onCommand(sender, args);
        } catch (Exception e) {
            if(!(e instanceof ArrayIndexOutOfBoundsException)){
                e.printStackTrace();
            }
            if (!Objects.equals(e.getMessage(), "FORMAT_ERROR")) {
                Quark.LOGGER.severe(e.getMessage());
            }
            this.sendExceptionMessage(sender);
        }
        return true;
    }

    @Override
    public @NotNull String getUsage() {
        return "";
    }

    @Override
    public @NotNull String getDescription() {
        return "";
    }

    @Override
    public @NotNull String getName() {
        return this.getClass().getAnnotation(QuarkCommand.class).name();
    }
}
