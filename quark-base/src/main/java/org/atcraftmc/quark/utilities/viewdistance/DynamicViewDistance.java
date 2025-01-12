package org.atcraftmc.quark.utilities.viewdistance;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.assertion.NumberLimitation;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.*;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.ArrayList;
import java.util.List;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(DynamicViewDistance.ViewDistanceCommand.class)
@QuarkModule(version = "1.0.0", compatBlackList = {APIProfile.BUKKIT, APIProfile.ARCLIGHT, APIProfile.SPIGOT})
public final class DynamicViewDistance extends PackageModule implements QuarkCommandExecutor {
    private final List<ViewDistanceStrategy> pipeline = new ArrayList<>();

    @Inject
    private LanguageEntry language;
    
    @Inject("-quark.viewdistance.other")
    private Permission setOtherPermission;

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));

        //this.pipeline.add(new NetworkPingStrategy());
        this.pipeline.add(new PlayerCountStrategy());
        this.pipeline.add(new CustomSettingStrategy());

        TaskService.async().timer("view-distance:calc", 1, 20, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                calculatePlayerViewDistance(p, false, false);
            }
        });
    }

    @Override
    public void checkCompatibility() {
        Compatibility.requireMethod(() -> Player.class.getMethod("setSendViewDistance", int.class));
        Compatibility.requireMethod(() -> Player.class.getMethod("setViewDistance", int.class));
        Compatibility.requireMethod(() -> Player.class.getMethod("getLastLogin"));
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));

        TaskService.async().cancel("view-distance:calc");

        int val = Bukkit.getViewDistance();
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                p.setViewDistance(val);
                p.setSendViewDistance(val);
            }catch (IllegalStateException ignored){
                //who cares?
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TaskService.global().delay(10, () -> this.calculatePlayerViewDistance(event.getPlayer(), true, false));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        int val = Bukkit.getViewDistance();
        event.getPlayer().setViewDistance(val);
        event.getPlayer().setSendViewDistance(val);
    }


    public void calculatePlayerViewDistance(final Player player, boolean remind, boolean forceRemind) {
        int value = Bukkit.getViewDistance();
        boolean programRemind = false;

        for (ViewDistanceStrategy strategy : pipeline) {
            value = strategy.determine(player, value);
            programRemind = strategy.remindPlayer(player, programRemind);
        }

        value = Math.max(2, Math.min(32, value));

        if ((programRemind && remind) || forceRemind) {
            this.language.sendMessage(player, "set-self", value);
        }

        player.setViewDistance(value);
        player.setSendViewDistance(value);
    }


    @Override
    public void execute(CommandExecution context) {
        String[] args = context.getArgs();
        CommandSender sender = context.getSender();

        int value = context.requireIntegerOrElse(0, -1, NumberLimitation.bound(2, 32));
        Player player;

        if (context.hasArgumentAt(1)) {
            context.requirePermission(this.setOtherPermission);
            player = context.requirePlayer(1);
        } else {
            player = context.requireSenderAsPlayer();
        }

        boolean isSelf = sender.getName().equals(player.getName());

        if (value == -1) {
            CustomSettingStrategy.clear(player);
            if (!isSelf) {
                this.language.sendMessage(sender, "reset-target", args[0]);
            }
            this.language.sendMessage(player, "reset-self");

            this.calculatePlayerViewDistance(player, false, false);
            return;
        }

        CustomSettingStrategy.set(player, value);
        if (!isSelf) {
            this.language.sendMessage(sender, "set-target", args[1], value);
        }
        this.language.sendMessage(player, "set-self", value);

        this.calculatePlayerViewDistance(player, false, false);
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "2", "4", "8", "16", "24", "32", "reset");
        suggestion.requireAnyPermission((s) -> s.suggestPlayers(1), this.setOtherPermission);
    }


    @QuarkCommand(name = "view-distance", permission = "+quark.viewdistance")
    public static final class ViewDistanceCommand extends ModuleCommand<DynamicViewDistance> {
        @Override
        public void init(DynamicViewDistance module) {
            setExecutor(module);
        }
    }
}
