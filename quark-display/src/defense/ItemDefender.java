package org.tbstcraft.quark.internal.defense;

import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.ModuleCommand;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.registry.TypeItem;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.tbstcraft.quark.module.PluginModule;

@TypeItem("quark_defense:item_defender")
public final class ItemDefender extends PluginModule {
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
                 {white}Item-Defender 1.4
                 {gray}  ——想拿非法物品？看我脸色再说
                 {gold}----------------------------------------------
                 {white}作者: {gray}GrassBlock2022
                 {white}版权: {gray}©FlybirdGames 2015-2023
                """).formatted(this.getModuleID(), this.getClass().getName()));
    }


    //event
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        this.checkEvent(event.getPlayer(), event.getPlayer().getInventory().getItem(event.getNewSlot()));
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        this.checkEvent(event.getPlayer(), event.getItem());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        this.checkEvent(event.getPlayer(), event.getItem());
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        this.checkEvent(event.getPlayer(), event.getMainHandItem());
        this.checkEvent(event.getPlayer(), event.getOffHandItem());
    }

    private void checkEvent(Player p, ItemStack stack) {
        if (stack == null) {
            return;
        }

        if (this.getConfig().isEnabled(this.getId(), "op_ignore") && p.isOp()) {
            return;
        }

        Material m = stack.getType();

        if (this.isItemIllegal(m)) {
            this.sendMessageTo(p, "illegal_item", m.name());
            p.getInventory().remove(m);

            String mode = this.getConfig().getConfigSection().getString("broadcast");
            if (Objects.equals(mode, "operator") || Objects.equals(mode, "all")) {
                this.broadcastMessage(mode.equals("operator"), "illegal_item_broadcast", p.getName(), m.name());
            }

            if (!this.getConfig().isEnabled(this.getId(), "record")) {
                return;
            }
            this.record("[%s] illegal player=%s item=%s".formatted(new SimpleDateFormat().format(new Date()), p.getName(), m.name()));
        }
        if (this.isItemWarning(m)) {
            this.sendMessageTo(p, "warning_item", m.name());

            String mode = this.getConfig().getConfigSection().getString("broadcast");
            if (Objects.equals(mode, "operator") || Objects.equals(mode, "all")) {
                this.broadcastMessage(mode.equals("operator"), "warning_item_broadcast", p.getName(), m.name());
            }

            if (!this.getConfig().isEnabled(this.getId(), "record")) {
                return;
            }
            this.record("[%s] warning player=%s item=%s".formatted(new SimpleDateFormat().format(new Date()), p.getName(), m.name()));
        }
    }

    private boolean isItemIllegal(Material material) {
        List<String> list = this.getConfig().getConfigSection().getStringList("illegal_list");
        return list.contains(material.getKey().getKey());
    }

    private boolean isItemWarning(Material material) {
        List<String> list = this.getConfig().getConfigSection().getStringList("warning_list");
        return list.contains(material.getKey().getKey());
    }

    @QuarkCommand(name = "item-defender", op = true)
    public static final class CommandHandler extends ModuleCommand<ItemDefender> {
        public CommandHandler(ItemDefender module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String arg1 = args[0];
            String arg2 = args[1];
            switch (arg1) {
                case "warning-list", "illegal-list" -> {
                    this.checkException(args.length == 3);
                    this.handleList(arg2, arg1, args[2], sender);
                }
                case "broadcast" -> {
                    this.checkException(args.length == 2);
                    this.handleBroadcastOption(arg2, sender);
                }
                case "record" -> {
                    this.checkException(args.length == 2);
                    this.handleRecordOption(arg2, sender);
                }
                case "op-ignore" -> {
                    this.checkException(args.length == 2);
                    this.handleOPIgnoreOption(arg2, sender);
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            if (args.length == 1) {
                tabList.add("warning-list");
                tabList.add("illegal-list");
                tabList.add("record");
                tabList.add("broadcast");
                tabList.add("op-ignore");
                return;
            }

            String arg1 = args[0];
            if (args.length == 2) {
                switch (arg1) {
                    case "warning-list", "illegal-list" -> {
                        tabList.add("add");
                        tabList.add("remove");
                        return;
                    }
                    case "record", "op-ignore" -> {
                        tabList.add("true");
                        tabList.add("false");
                        return;
                    }
                    case "broadcast" -> {
                        tabList.add("none");
                        tabList.add("operator");
                        tabList.add("all");
                        return;
                    }
                }
                return;
            }

            if (args.length == 3) {
                String arg2 = args[1];
                switch (arg2) {
                    case "add" -> {
                        for (Material m : Material.values()) {
                            tabList.add(m.getKey().getKey());
                        }
                    }
                    case "remove" -> {
                        List<String> list = null;
                        if (Objects.equals(arg1, "warning_list")) {
                            list = this.getModule().getConfig().getConfigSection().getStringList("warning_list");
                            tabList.addAll(list);
                        } else if (Objects.equals(arg1, "illegal_list")) {
                            list = this.getModule().getConfig().getConfigSection().getStringList("illegal_list");
                        }
                        if (list == null) {
                            return;
                        }
                        tabList.addAll(list);
                    }
                }
            }
        }

        @Override
        public String getUsage() {
            return """
                    以下是各个子命令(蓝色填入整数, 黄色填入名称, 绿色填入tab指定的选项):
                    {white} - 添加物品: /item-defender {green}add [list] {yellow}[id]
                    {white} - 移除物品: /item-defender {green}remove [list] {yellow}[id]
                    {white} - 查看区域: /item-defender {green}op-ignore [operation]
                    {white} - 记录开关: /item-defender {green}record [operation]
                    {white} - 广播开关: /item-defender {green}broadcast [operation]
                     """;
        }

        @Override
        public String getDescription() {
            return "对物品管理进行指定的操作。";
        }


        public void handleList(String option, String listId, String item, CommandSender sender) {
            List<String> list;
            list = this.getModule().getConfig().getConfigSection().getStringList(listId);

            if (option.equals("add")) {
                if (!list.contains(item)) {
                    list.add(item);
                }
                this.getModule().getConfig().getConfigSection().set(listId, list);
                this.getModule().sendMessageTo(sender, "command_" + listId + "_add", item);
            }
            if (option.equals("remove")) {
                list.remove(item);
                this.getModule().getConfig().getConfigSection().set(listId, list);
                this.getModule().sendMessageTo(sender, "command_" + listId + "_add", item);
            }
            this.getModule().saveConfig();
        }

        public void handleOPIgnoreOption(String option, CommandSender sender) {
            if (this.isBooleanOption(option)) {
                this.getModule().getConfig().getConfigSection().set("op_ignore", option);
                this.getModule().sendMessageTo(sender, "command_op_ignore_set", option);
                this.getModule().reloadConfig();
            } else {
                this.getModule().sendMessageTo(sender, "command_exception");
            }
        }

        public void handleRecordOption(String option, CommandSender sender) {
            this.checkException(this.isBooleanOption(option));
            this.getModule().getConfig().getConfigSection().set("record", option);

            if (option.equals("true")) {
                this.getModule().openRecordStream();
            }
            if (option.equals("false")) {
                this.getModule().closeRecordStream();
            }

            this.getModule().sendMessageTo(sender, "command_record_set", option);
            this.getModule().reloadConfig();
        }

        public void handleBroadcastOption(String option, CommandSender sender) {
            if ((!Objects.equals(option, "none") && !Objects.equals(option, "operator") && !Objects.equals(option, "all"))) {
                this.getModule().sendMessageTo(sender, "command_exception");
                return;
            }
            this.getModule().getConfig().getConfigSection().set("broadcast", option);
            this.getModule().sendMessageTo(sender, "command_broadcast_set", option);
            this.getModule().reloadConfig();
        }
    }
}