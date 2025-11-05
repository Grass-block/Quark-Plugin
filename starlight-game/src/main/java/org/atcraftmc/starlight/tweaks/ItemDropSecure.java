package org.atcraftmc.starlight.tweaks;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ApplicationModule(id = "item-drop-secure")
@CommandProvider(ItemDropSecure.DropInsecureUnlockCommand.class)
@AutoRegister(Registrations.SERVER_EVENT)
public final class ItemDropSecure extends SLPackageModule implements PluginCommandExecutor {
    private final Set<String> unlocks = new HashSet<>();

    @Inject
    private LanguageEntry language;

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        var drop = event.getItemDrop();
        var player = event.getPlayer();
        var id = drop.getItemStack().getType().getKey().toString();

        if (this.unlocks.contains(player.getName())) {
            return;
        }

        if (Arrays.stream(player.getInventory().getStorageContents()).allMatch(Objects::nonNull)) {
            return;
        }

        for (var s : ConfigAccessor.configList(config(), "list", String.class)) {
            if (!id.contains(s)) {
                continue;
            }
            event.setCancelled(true);
            MessageAccessor.send(this.language, player, "warn");
            return;
        }
    }

    @Override
    public void execute(CommandExecution context) {
        var player = context.requireSenderAsPlayer();
        var ticks = ConfigAccessor.getInt(config(), "unlock-time");

        this.unlocks.add(player.getName());
        MessageAccessor.send(this.language, player, "unlock", ticks / 20);
        TaskService.global().delay(ticks, () -> {
            this.unlocks.remove(player.getName());
            MessageAccessor.send(this.language, player, "unlock-end");
        });
    }

    @QuarkCommand(name = "unlock-drop", permission = "+quark.dropunlock", playerOnly = true)
    public static final class DropInsecureUnlockCommand extends ModuleCommand<ItemDropSecure> {
        @Override
        public void init(ItemDropSecure module) {
            setExecutor(module);
        }
    }
}
