package org.tbstcraft.quark.utilities;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;

import java.util.List;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({BlockUpdateLocker.BlockUpdateLockerCommand.class})
@QuarkModule(version = "1.0.0")
public final class BlockUpdateLocker extends PackageModule {
    private boolean locked = false;

    @EventHandler
    public void handle(BlockPistonExtendEvent event) {
        event.setCancelled(this.locked);
    }

    @EventHandler
    public void handle(BlockPistonRetractEvent event) {
        event.setCancelled(this.locked);
    }

    @EventHandler
    public void handle(BlockDispenseEvent event) {
        event.setCancelled(this.locked);
    }

    @EventHandler
    public void handle(BlockRedstoneEvent event) {
        if (this.locked) event.setNewCurrent(event.getOldCurrent());
    }

    @EventHandler
    public void handle(BlockSpreadEvent event) {
        event.setCancelled(this.locked);
    }

    @EventHandler
    public void handle(BlockPhysicsEvent event) {
        event.setCancelled(this.locked);
    }

    @EventHandler
    public void handle(BlockGrowEvent event) {
        event.setCancelled(this.locked);
    }

    @QuarkCommand(name = "block-update-locker", op = true)
    public static final class BlockUpdateLockerCommand extends ModuleCommand<BlockUpdateLocker> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "lock" -> {
                    this.getModule().locked = true;
                    this.getLanguage().sendMessage(sender,"lock");
                }
                case "unlock" -> {
                    this.getLanguage().sendMessage(sender,"unlock");
                    this.getModule().locked = false;
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("lock");
                tabList.add("unlock");
            }
        }
    }
}
