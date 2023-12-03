package org.tbstcraft.quark.internal.optimize;

import org.tbstcraft.quark.command.AbstractCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.ModuleCommand;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.registry.TypeItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;
import java.util.Objects;

@TypeItem("quark_optimize:maintenance_mode")
public final class MaintenanceMode extends PluginModule {
    private final AbstractCommand command = new CommandHandler(this);

    @Override
    public void onEnable() {
        CommandManager.registerCommand(this.command);
        this.registerListener();
    }

    @Override
    public void onDisable() {
        CommandManager.unregisterCommand(this.command);
        this.unregisterListener();
    }

    @Override
    public void displayInfo(CommandSender sender) {
        sender.sendMessage(BukkitUtil.formatChatComponent("""
                 {white}Maintenance-Mode 1.0
                 {gray}  ——好好维护吧，别担心有人进来
                 {gold}----------------------------------------------
                 {white}作者: {gray}GrassBlock2022
                 {white}版权: {gray}©ProtonGames 2023
                 {white}ID: {gray}%s
                """).formatted(this.getModuleID(), this.getClass().getName()));
    }


    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if(!this.getConfig().isEnabled(this.getId(), "mode")){
            return;
        }
        try {
            if (Objects.requireNonNull(Bukkit.getOfflinePlayer(event.getName())).isOp() && this.getConfig().isEnabled(this.getId(), "allow_op")) {
                return;
            }
        } catch (NullPointerException ignored) {}
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.getConfig().getMessage("kicked"));
    }

    public void kickAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp() && this.getConfig().isEnabled(this.getId(), "allow_op")) {
                continue;
            }
            player.kickPlayer(this.getConfig().getMessage("kicked"));
        }
    }

    @QuarkCommand(name = "maintenance", op = true)
    public static final class CommandHandler extends ModuleCommand<MaintenanceMode> {
        public CommandHandler(MaintenanceMode module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.checkException(args.length == 2);
            String arg2 = args[1];
            this.checkException(this.isBooleanOption(arg2));
            switch (args[0]) {
                case "mode" -> {
                    this.getModule().getConfig().getConfigSection().set("on", arg2);
                    this.getModule().sendMessageTo(sender, "command_mode_set", arg2);
                    this.getModule().reloadConfig();
                    if(Objects.equals(arg2, "true")){
                        this.getModule().kickAll();
                    }
                }
                case "allow_op" -> {
                    this.getModule().getConfig().getConfigSection().set("allow_op", arg2);
                    this.getModule().sendMessageTo(sender, "command_allow_op_set", arg2);
                    this.getModule().reloadConfig();
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("allow_op");
                tabList.add("mode");
                return;
            }
            tabList.add("true");
            tabList.add("false");
        }
    }
}
