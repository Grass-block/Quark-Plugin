package org.atcraftmc.starlight.management;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.data.storage.StorageTable;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.util.CachedInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Date;
import java.util.Objects;

@CommandProvider({Mute.MuteCommand.class, Mute.UnmuteCommand.class})
@SLModule(id = "chat-mute", version = "1.0.2")
@Components(Mute.PaperListener.class)
@AutoRegister(Registers.BUKKIT_EVENT)
public final class Mute extends PackageModule implements Listener {
    public static final String DATA_ENTRY_ID = "starlight:mute";

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatting(AsyncPlayerChatEvent event) {
        this.checkEvent(event.getPlayer(), event, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectCommand(PlayerCommandPreprocessEvent event) {
        if (!(event.getMessage().contains("say") || event.getMessage().contains("tell"))) {
            return;
        }
        this.checkEvent(event.getPlayer(), event, false);
    }

    public StorageTable getBanEntryFor(String id) {
        var entry = PlayerDataService.get(id);

        if (!entry.hasKey(DATA_ENTRY_ID)) {
            var table = new StorageTable();

            table.setLong("expired", 0);
            table.setString("reason", "N/A");
            table.setBoolean("banned", false);

            entry.setTable(DATA_ENTRY_ID, table);
            entry.save();
        }

        return entry.getTable(DATA_ENTRY_ID);
    }


    public void checkEvent(Player p, Cancellable event, boolean silent) {
        var data = getBanEntryFor(p.getName());

        if (!data.getBoolean("banned")) {
            return;
        }

        var expire = data.getLong("expired");
        var expired = SharedObjects.DATE_FORMAT.format(new Date(expire));

        if (expire != 0 && System.currentTimeMillis() > expire) {
            return;
        }

        if (expire == Long.MAX_VALUE) {
            expired = "9999-12-31 23:59:59";
        }
        event.setCancelled(true);

        if (silent) {
            return;
        }

        MessageAccessor.send(this.getLanguage(), Objects.requireNonNull(p.getPlayer()), "message-banned", expired);
    }


    public void mute(String name, long expire, String reason, CommandSender operator) {
        var entry = getBanEntryFor(name);

        entry.setLong("expired", expire);
        entry.setString("reason", reason);
        entry.setBoolean("banned", true);

        entry.save();

        var player = Bukkit.getPlayerExact(name);
        var expired = SharedObjects.DATE_FORMAT.format(new Date(expire));

        if (expire == Long.MAX_VALUE) {
            expired = "9999-12-31 23:59:59";
        }

        this.getLanguage().item("add").send(operator.getName(), name, expired, reason);

        if (player == null || player.getName().equals(operator.getName())) {
            return;
        }

        this.getLanguage().item("add-target").send(player, operator.getName(), reason, expired);
    }

    public boolean unmute(String name, CommandSender operator) {
        var entry = getBanEntryFor(name);

        if (!entry.getBoolean("banned")) {
            return false;
        }

        entry.setBoolean("banned", false);
        entry.save();

        var player = Bukkit.getPlayerExact(name);
        if (player != null && !player.getName().equals(operator.getName())) {
            this.getLanguage().item("remove-target").send(player, operator.getName());
        }

        this.getLanguage().item("remove").send(operator.getName(), name);

        return true;
    }


    @QuarkCommand(name = "unmute", permission = "-quark.unmute")
    public static final class UnmuteCommand extends ModuleCommand<Mute> {
        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, ModuleDataService.getEntry(this.getModuleId()).getTagMap().keySet());
        }

        @Override
        public void execute(CommandExecution context) {
            if (!getModule().unmute(context.requireArgumentAt(0), context.getSender())) {
                MessageAccessor.send(this.getLanguage(), context.getSender(), "message-unmuted", context.requireArgumentAt(0));
            }
        }
    }


    @QuarkCommand(name = "mute", op = true)
    public static final class MuteCommand extends ModuleCommand<Mute> {

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, CachedInfo.getAllPlayerNames());
            suggestion.suggest(1, "time[seconds]", "forever");
            suggestion.suggest(2, "<reason>");
        }

        @Override
        public void execute(CommandExecution context) {
            var length = -1;
            var target = context.requireArgumentAt(0);
            var reason = context.requireRemainAsParagraph(2, true);

            if (!Objects.equals(context.requireArgumentAt(1), "forever")) {
                length = context.requireArgumentInteger(1, NumberLimitation.moreThan(0));
            }

            var expire = length != -1 ? System.currentTimeMillis() + length * 1000L : 0;

            if (expire == 0) {
                expire = Long.MAX_VALUE;
            }

            getModule().mute(target, expire, reason, context.getSender());
        }
    }

    @AutoRegister(Registers.BUKKIT_EVENT)
    public static final class PaperListener extends ModuleComponent<Mute> {

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("io.papermc.paper.event.player.AsyncChatEvent"));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAsyncChat(AsyncPlayerChatEvent event) {
            this.parent.checkEvent(event.getPlayer(), event, true);
        }
    }

}