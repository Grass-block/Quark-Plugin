package org.tbstcraft.quark.internal.defense;

import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.ModuleCommand;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.registry.TypeItem;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@TypeItem("quark_defense:explosion_defender")
public final class ExplosionDefender extends PluginModule {
    private final CommandHandler command = new CommandHandler(this);

    @Override
    public void onEnable() {
        if (this.getConfig().isEnabled(this.getId(), "record")) {
            this.openRecordStream();
        }
        this.registerListener();
        this.registerCommand(this.command);
    }

    @Override
    public void onDisable() {
        this.closeRecordStream();
        this.unregisterCommand(this.command);
        this.unregisterListener();
    }

    @Override
    public void displayInfo(CommandSender sender) {
        sender.sendMessage(BukkitUtil.formatChatComponent("""
                 {white}Explosion-Defender 1.3
                 {gray}  ——撅飞一切会爆炸的东西，包括实体和方块
                 {gold}----------------------------------------------
                 {white}作者: {gray}GrassBlock2022
                 {white}版权: {gray}©FlybirdGames 2015-2023
                 {white}ID: {gray}%s
                """).formatted(
                this.getModuleID(),
                this.getClass().getName()
        ));
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Block b = event.getBlock();

        event.setCancelled(true);
        if (Objects.equals(this.getConfig().getConfigSection().getString("mode"), "block")) {
            Objects.requireNonNull(b.getLocation().getWorld()).createExplosion(b.getLocation(), 4, true, false);
        }

        switch (Objects.requireNonNull(this.getConfig().getConfigSection().getString("broadcast"))) {
            case "operator", "all" ->
                    this.broadcastMessage(Objects.equals(this.getConfig().getConfigSection().getString("broadcast"), "operator"), "block_exploded", Objects.requireNonNull(b.getLocation().getWorld()).getName(), b.getLocation().getBlockX(), b.getLocation().getBlockY(), b.getLocation().getBlockZ(), b.getType().getKey().toString());
            default -> {
            }
        }

        if (this.getConfig().isEnabled(this.getId(), "record")) {
            this.record("[%s]world:%s pos:%s,%s,%s type:%s".formatted(new SimpleDateFormat().format(new Date()), b.getWorld().getName(), b.getLocation().getBlockX(), b.getLocation().getBlockY(), b.getLocation().getBlockZ(), b.getType().getKey().toString()));
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity e = event.getEntity();

        event.setCancelled(true);
        if (Objects.equals(this.getConfig().getConfigSection().getString("mode"), "block")) {
            e.getWorld().createExplosion(event.getLocation(), 4, false, false, e);
        }

        switch (Objects.requireNonNull(this.getConfig().getConfigSection().getString("broadcast"))) {
            case "operator", "all" ->
                    this.broadcastMessage(Objects.equals(this.getConfig().getConfigSection().getString("broadcast"), "operator"), "entity_exploded", Objects.requireNonNull(event.getLocation().getWorld()).getName(), event.getLocation().getBlockX(), event.getLocation().getBlockY(), event.getLocation().getBlockZ(), e.getType().getKey().toString());
            default -> {
            }
        }

        if (this.getConfig().isEnabled(this.getId(), "record")) {
            this.record("[%s]world:%s pos:%s,%s,%s type:%s".formatted(new SimpleDateFormat().format(new Date()), e.getWorld().getName(), e.getLocation().getBlockX(), e.getLocation().getBlockY(), e.getLocation().getBlockZ(), e.getType().getKey().toString()));
        }
    }

    @QuarkCommand(name = "explosion-defender", op = true)
    public static final class CommandHandler extends ModuleCommand<ExplosionDefender> {
        public CommandHandler(ExplosionDefender module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.checkException(args.length == 2);
            String arg2 = args[1];
            switch (args[0]) {
                case "mode" -> {
                    if ((!Objects.equals(arg2, "cancel") && !Objects.equals(arg2, "block"))) {
                        this.sendExceptionMessage(sender);
                        return;
                    }
                    if (Objects.equals(arg2, "block") && (BukkitUtil.getBukkitVersion() < 16)) {
                        this.getModule().sendMessageTo(sender, "api_unsupported");
                        return;
                    }
                    this.getModule().getConfig().getConfigSection().set("mode", arg2);
                    this.getModule().sendMessageTo(sender, "command_mode_set", arg2);
                    this.getModule().reloadConfig();
                }
                case "broadcast" -> {
                    if ((!Objects.equals(arg2, "none") && !Objects.equals(arg2, "operator") && !Objects.equals(arg2, "all"))) {
                        this.sendExceptionMessage(sender);
                        return;
                    }
                    this.getModule().getConfig().getConfigSection().set("broadcast", arg2);
                    this.getModule().sendMessageTo(sender, "command_broadcast_set", arg2);
                    this.getModule().reloadConfig();
                }
                case "record" -> {
                    this.checkException(isBooleanOption(arg2));
                    this.getModule().getConfig().getConfigSection().set("record", arg2);
                    this.getModule().sendMessageTo(sender, "command_record_set", arg2);
                    this.getModule().reloadConfig();

                    if (arg2.equals("true")) {
                        this.getModule().openRecordStream();
                    }
                    if (arg2.equals("false")) {
                        this.getModule().closeRecordStream();
                    }
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("mode");
                tabList.add("record");
                tabList.add("broadcast");
                return;
            }

            switch (args[0]) {
                case "mode" -> {
                    tabList.add("cancel");
                    tabList.add("block");
                }
                case "record" -> {
                    tabList.add("on");
                    tabList.add("off");
                }
                case "broadcast" -> {
                    tabList.add("none");
                    tabList.add("operator");
                    tabList.add("all");
                }
            }
        }

        @Override
        public String getUsage() {
            return """
                    以下是各个子命令(蓝色填入整数, 黄色填入名称, 绿色填入tab指定的选项):
                    {white} - 保护模式: /explosion-defender {green}mode [operation]
                    {white} - 记录开关: /explosion-defender {green}record [operation]
                    {white} - 广播开关: /explosion-defender {green}broadcast [operation]
                     """;
        }

        @Override
        public String getDescription() {
            return "为爆炸保护设定模式";
        }
    }
}