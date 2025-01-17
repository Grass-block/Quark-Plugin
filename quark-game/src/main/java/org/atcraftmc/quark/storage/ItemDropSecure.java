package org.atcraftmc.quark.storage;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommandExecutor;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@QuarkModule
@CommandProvider(ItemDropSecure.DropInsecureUnlockCommand.class)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ItemDropSecure extends PackageModule implements QuarkCommandExecutor {
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

        for (var s : getConfig().getList("list")) {
            if (!id.contains(s)) {
                continue;
            }
            event.setCancelled(true);
            this.language.sendMessage(player, "warn");
            return;
        }
    }

    @Override
    public void execute(CommandExecution context) {
        var player = context.requireSenderAsPlayer();
        var ticks = getConfig().getInt("unlock-time");

        this.unlocks.add(player.getName());
        this.language.sendMessage(player, "unlock", ticks / 20);
        TaskService.global().delay(ticks, () -> {
            this.unlocks.remove(player.getName());
            this.language.sendMessage(player, "unlock-end");
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
