package org.tbstcraft.quark.internal.defense;

import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.util.Region;
import org.tbstcraft.quark.util.registry.TypeItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Objects;

@TypeItem("quark_defense:we_size_defender")
public class WorldEditSectionSizeDefender extends PluginModule {
    private final CommandHandler handler = new CommandHandler(this);

    @Override
    public void onEnable() {
        this.registerListener();
        CommandManager.registerCommand(this.handler);
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
        CommandManager.unregisterCommand(this.handler);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            return;
        }
        if (event.getMessage().startsWith("//pos1")) {
            return;
        }
        if (event.getMessage().startsWith("//pos2")) {
            return;
        }
        if (event.getMessage().startsWith("//")) {
            Player player = event.getPlayer();
            if (Objects.requireNonNull(player.getPlayer()).isOp()) {
                return;
            }
            Region r = SharedObjects.WE_SESSION_TRACKER.getRegion(player);
            if (r.asAABB().getMaxWidth() > this.getConfig().getRootSection().getInt("size")) {
                event.setCancelled(true);
                this.getLanguage().sendMessageTo(player, this.getId(), "interact_blocked_we");
            }
            if (!this.getConfig().isEnabled(this.getId(), "record")) {
                return;
            }
            this.getRecordEntry().record("player:%s world:%s session:%s", player.getName(), Objects.requireNonNull(event.getPlayer().getEyeLocation().getWorld()).getName(), r.toString());
        }
    }

    @QuarkCommand(name = "we-size-def", op = true)
    private static class CommandHandler extends ModuleCommand<WorldEditSectionSizeDefender> {
        protected CommandHandler(WorldEditSectionSizeDefender module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String arg2 = args[1];
            switch (args[0]) {
                case "set" -> {
                    this.getModule().getConfig().getRootSection().set("size", arg2);
                    this.getModule().getConfig().reload();
                    PluginModule module = this.getModule();
                    this.getModule().getLanguage().sendMessageTo(sender, module.getId(), "command_size_set", arg2);
                }
                case "record" -> {
                    this.checkException(args.length == 2);
                    this.checkException(isBooleanOption(arg2));
                    this.getModule().getConfig().getRootSection().set("record", arg2);
                    PluginModule module = this.getModule();
                    this.getModule().getLanguage().sendMessageTo(sender, module.getId(), "command_record_set", arg2);
                    this.getModule().getConfig().reload();
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("set");
                tabList.add("record");
                return;
            }
            if ("record".equals(args[0])) {
                if (args.length + 1 > 2) {
                    return;
                }
                tabList.add("true");
                tabList.add("false");
            }
        }

        @Override
        public @NotNull String getUsage() {
            return """
                    以下是各个子命令(蓝色填入整数, 黄色填入名称, 绿色填入tab指定的选项):
                    {white} - 设置大小: /we-size-def {green}set {aqua}[size]
                    {white} - 记录开关: /we-size-def {green}record [operation]
                     """;
        }

        @Override
        public @NotNull String getDescription() {
            return "对we选区大小进行指定的操作。";
        }
    }
}
