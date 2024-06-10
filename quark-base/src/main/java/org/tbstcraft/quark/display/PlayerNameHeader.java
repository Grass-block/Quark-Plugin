package org.tbstcraft.quark.display;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.data.config.Queries;
import org.tbstcraft.quark.framework.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.data.ModuleDataService;
import org.tbstcraft.quark.util.container.CachedInfo;
import org.tbstcraft.quark.util.platform.APIProfileTest;
import org.tbstcraft.quark.util.platform.PlayerUtil;
import org.tbstcraft.quark.util.text.TextBuilder;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule
@QuarkCommand(name = "header", op = true)
public final class PlayerNameHeader extends CommandModule {
    @Inject
    private LanguageEntry language;


    @Override
    public void enable() {
        super.enable();
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.attach(p);
        }
    }

    @Override
    public void disable() {
        super.disable();
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.detach(p);
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player p = PlayerUtil.strictFindPlayer(args[1]);
        NBTTagCompound entry = ModuleDataService.getEntry(this.getId());
        if (Objects.equals(args[0], "set")) {
            entry.setString(args[1], args[2]);
            this.language.sendMessage(sender, "set-header", args[1], args[2]);
        }
        if (Objects.equals(args[0], "clear")) {
            entry.remove(args[1]);
            this.language.sendMessage(sender, "clear-header", args[1]);

        }
        if (p != null && p.isOnline()) {
            this.attach(p);
        }
        ModuleDataService.save(this.getId());
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("set");
            tabList.add("clear");
        }
        if (buffer.length == 2) {
            tabList.addAll(CachedInfo.getAllPlayerNames());
        }
        if (buffer.length == 3 && Objects.equals(buffer[0], "set")) {
            tabList.add("<header>");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.attach(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        this.detach(event.getPlayer());
    }

    public void attach(Player p) {
        Component name = getPlayerName(p);
        if (APIProfileTest.isPaperCompat()) {
            try {
                p.playerListName(name);
            } catch (IllegalArgumentException ignored) {
            }
            p.customName(name);
            p.displayName(name);
            return;
        }
        String _name = LegacyComponentSerializer.legacySection().serialize(name);
        p.setDisplayName(_name);
        try {
            p.setPlayerListName(_name);
        } catch (IllegalArgumentException ignored) {
        }
        p.setCustomName(_name);
        p.setCustomNameVisible(true);
    }

    public void detach(Player p) {
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());
        p.setCustomName(null);
    }

    public Component getPlayerName(Player player) {
        String header;
        String name = player.getName();
        NBTTagCompound tag = ModuleDataService.getEntry("player-name-header");
        if (tag.hasKey(name)) {
            header = tag.getString(name);
        } else {
            if (player.isOp()) {
                header = getConfig().getString("op-header");
            } else {
                header = getConfig().getString("player-header");
            }
        }
        String template = this.getConfig().getString("template");
        if (template == null) {
            return Component.text(player.getName());
        }
        return TextBuilder.buildComponent(Queries.GLOBAL_TEMPLATE_ENGINE.handle(template.replace("{player}", player.getName()).replace("{header}", header + TextBuilder.EMPTY_COMPONENT)));
    }
}
