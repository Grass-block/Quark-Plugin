package org.tbstcraft.quark.framework.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.service.PermissionService;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("NullableProblems")
public abstract class AbstractCommand extends Command implements CommandExecuter {
    private final Map<String, AbstractCommand> subCommands = new HashMap<>();
    private Command covered;

    protected AbstractCommand() {
        super("");
    }

    protected void init() {
        this.setName(this.getName());
        for (Class<? extends AbstractCommand> clazz:this.getDescriptor().subCommands()){
            try {
                this.registerSubCommand(clazz.getDeclaredConstructor().newInstance());
            }catch (Exception e){
                ExceptionUtil.log(e);
            }
        }
        String permission = this.getPermission();
        if (permission.equals(QuarkCommand.NO_INFO)) {
            return;
        }
        String[] perms = this.getPermission().split(";");
        for (String str : perms) {
            if (Objects.equals(str, "")) {
                continue;
            }
            PermissionService.createPermission(str);
        }
    }

    //description
    @Override
    public String getName() {
        return this.getDescriptor().name();
    }

    public QuarkCommand getDescriptor() {
        return this.getClass().getAnnotation(QuarkCommand.class);
    }

    public <V> V optionalDescriptorInfo(Function<QuarkCommand, V> consumer, V fallback) {
        QuarkCommand desc = this.getDescriptor();
        if (desc == null) {
            return fallback;
        }
        return consumer.apply(desc);
    }

    public boolean isOP() {
        return this.getDescriptor() == null || getDescriptor().op();
    }

    public final boolean isPlayerOnly() {
        return this.getDescriptor() != null && getDescriptor().playerOnly();
    }

    public final boolean isEventBased() {
        return this.optionalDescriptorInfo(QuarkCommand::eventBased, false);
    }

    @Override
    public final @NotNull String getPermission() {
        return optionalDescriptorInfo(QuarkCommand::permission, QuarkCommand.NO_INFO);
    }

    @Override
    public final String getUsage() {
        return optionalDescriptorInfo(QuarkCommand::usage, QuarkCommand.NO_INFO);
    }

    @Override
    public final String getDescription() {
        return optionalDescriptorInfo(QuarkCommand::description, QuarkCommand.NO_INFO);
    }

    @Override
    public final List<String> getAliases() {
        return optionalDescriptorInfo((d) -> List.of(d.aliases()), List.of());
    }


    //error message
    public final void sendExceptionMessage(CommandSender sender) {
        Quark.LANGUAGE.sendMessageTo(sender, "command", "exception");
    }

    public final void sendPermissionMessage(CommandSender sender) {
        Quark.LANGUAGE.sendMessageTo(sender, "command", "lack_permission");
    }

    public final void sendPlayerOnlyMessage(CommandSender sender) {
        Quark.LANGUAGE.sendMessageTo(sender, "command", "player_only");
    }


    //execute
    @Override
    public final List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> tabList = new TabList();
        if (this.validateExecutable(sender, false)) {
            return tabList;
        }

        if (args.length > 1) {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
            for (String s : this.subCommands.keySet()) {
                if (!args[0].equals(s)) {
                    continue;
                }
                return subCommands.get(s).tabComplete(sender, "", subArgs);
            }
        }
        if (args.length == 1) {
            tabList.addAll(this.subCommands.keySet());
        }

        this.onTab(sender, args, tabList);
        return tabList;
    }

    @Override
    public final List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) {
        return this.tabComplete(sender, alias, args);
    }

    @Override
    public final boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (this.validateExecutable(sender, true)) {
            return true;
        }

        if (args.length >= 1) {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
            for (String s : this.subCommands.keySet()) {
                if (!args[0].equals(s)) {
                    continue;
                }
                return subCommands.get(s).execute(sender, "", subArgs);
            }
        }

        try {
            this.onCommand(sender, args);
        } catch (Exception e) {
            if ((e instanceof ArrayIndexOutOfBoundsException)) {
                this.sendExceptionMessage(sender);
                Quark.LOGGER.info("none-match arguments: " + Arrays.toString(args));
                return false;
            }
            if (!Objects.equals(e.getMessage(), "FORMAT_ERROR")) {
                Quark.LOGGER.severe(e.getMessage());
            }
            ExceptionUtil.log(e);
            this.sendExceptionMessage(sender);
        }
        return true;
    }

    private boolean validateExecutable(CommandSender sender, boolean msg) {
        if (!(sender instanceof Player) && this.isPlayerOnly()) {
            if (msg) {
                this.sendPlayerOnlyMessage(sender);
            }
            return true;
        }
        String permission = this.getPermission();
        if (permission.equals(QuarkCommand.NO_INFO)) {
            if (this.isOP() && !sender.isOp()) {
                if (msg) {
                    this.sendPermissionMessage(sender);
                }
                return true;
            }
        }else {
            String[] perms = this.getPermission().split(";");
            if (perms.length == 0) {
                return true;
            }
            for (String str : perms) {
                if (Objects.equals(str, "")) {
                    continue;
                }
                if (!sender.hasPermission(str.substring(1))) {
                    if (msg) {
                        this.sendPermissionMessage(sender);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    protected final void checkException(boolean predict) {
        if (predict) {
            return;
        }
        throw new RuntimeException("FORMAT_ERROR");
    }


    //sub-command
    public final void registerSubCommand(AbstractCommand command) {
        this.subCommands.put(command.getName(), command);
    }

    public final void unregisterSubCommand(AbstractCommand command) {
        this.unregisterSubCommand(command.getName());
    }

    public final void unregisterSubCommand(String id) {
        this.subCommands.remove(id);
    }


    public Command getCoveredCommand() {
        return null;
    }

    public Command getCovered() {
        return covered;
    }

    public Map<String, AbstractCommand> getSubCommands() {
        return subCommands;
    }

    public void fetchCovered() {
        this.covered = getCoveredCommand();
    }


    @Override
    public final boolean testPermissionSilent(@NotNull CommandSender target) {
        return true;
    }
}
