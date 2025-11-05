package org.atcraftmc.starlight.management;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.subcomponent.ComponentProvider;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.core.data.BanEntryService;
import org.atcraftmc.starlight.core.data.flex.TableColumn;
import org.atcraftmc.starlight.core.objects.BanEntry;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.data.storage.DataEntry;
import org.atcraftmc.starlight.data.storage.StorageTable;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLModuleComponent;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.migration.QuarkDataImporter;
import org.atcraftmc.starlight.util.CachedInfo;
import org.bukkit.OfflinePlayer;
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
import java.util.UUID;

@CommandProvider({Mute.MuteCommand.class, Mute.UnmuteCommand.class})
@ApplicationModule(id = "mute", version = "1.0.2")
@ComponentProvider(Mute.PaperListener.class)
@AutoRegister(Registrations.SERVER_EVENT)
public final class Mute extends SLPackageModule implements Listener {
    public static final String DATA_ENTRY_ID = "starlight:mute";
    public static final TableColumn<DataEntry> MUTE_DATA = TableColumn.dom("chat_mute");


    @Inject
    private LanguageEntry language;

    @Inject("starlight:shared/sl_mute")
    private BanEntryService muteData;

    @Override
    public void enable() throws Exception {
        QuarkDataImporter.registerPlayerDataHandler("starlight:mute", (uuid, nbt) -> {
            if (!nbt.hasKey(DATA_ENTRY_ID)) {
                return;
            }
            var e2 = MUTE_DATA.get(PlayerDataService.PLAYER_SHARED, uuid);

            e2.getTagMap().clear();
            e2.getTagMap().putAll(nbt.getCompoundTag(DATA_ENTRY_ID).getTagMap());

            e2.save();
        });
    }

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

    public StorageTable getBanEntryFor(UUID id) {
        var entry = MUTE_DATA.get(PlayerDataService.PLAYER_SHARED, id);

        if (!entry.hasKey("banned")) {
            entry.setLong("expired", 0);
            entry.setString("reason", "N/A");
            entry.setBoolean("banned", false);

            entry.save();
        }

        return entry;
    }


    public void checkEvent(Player p, Cancellable event, boolean silent) {
        if(!this.muteData.isBanned(p.getUniqueId())){
            return;
        }

        var entry = this.muteData.getValid(p.getUniqueId()).get(0);

        var expire = entry.getExpires();
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

        MessageAccessor.send(this.language, Objects.requireNonNull(p.getPlayer()), "message-banned", expired);
    }


    public void mute(OfflinePlayer p, long expire, String reason, CommandSender operator) {
        this.muteData.add(new BanEntry(UUID.randomUUID(),p.getUniqueId(),expire,reason,operator.getName()));

        var player = p.getPlayer();
        var expired = SharedObjects.DATE_FORMAT.format(new Date(expire));

        if (expire == Long.MAX_VALUE) {
            expired = "9999-12-31 23:59:59";
        }

        this.language.item("add").send(operator, p.getName(), expired, reason);

        if (player == null || player.getName().equals(operator.getName())) {
            return;
        }

        this.language.item("add-target").send(player, operator.getName(), reason, expired);
    }

    public boolean unmute(OfflinePlayer p, CommandSender operator) {
        this.muteData.pardon(p.getUniqueId());

        var player = p.getPlayer();
        if (player != null && !player.getName().equals(operator.getName())) {
            this.language.item("remove-target").send(player, operator.getName());
        }

        this.language.item("remove").send(operator, p.getName());

        return true;
    }


    @QuarkCommand(name = "unmute", permission = "-quark.unmute")
    public static final class UnmuteCommand extends ModuleCommand<Mute> {
        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggestPlayers(0);
        }

        @Override
        public void execute(CommandExecution context) {
            if (!getModule().unmute(context.requireOfflinePlayer(0), context.getSender())) {
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
            var target = context.requireOfflinePlayer(0);
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

    @AutoRegister(Registrations.SERVER_EVENT)
    public static final class PaperListener extends SLModuleComponent<Mute> {

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