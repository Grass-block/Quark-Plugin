package org.tbstcraft.quark.internal.data;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.util.DataFix;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.platform.BukkitUtil;

import java.io.File;

@QuarkService(id = "player-data")
public interface PlayerDataService extends Service {

    @ServiceInject
    ServiceHolder<PlayerDataService> INSTANCE = new ServiceHolder<>();

    @ServiceInject
    static void start() {
        INSTANCE.set(create(FilePath.pluginFolder("Quark") + "/data/player"));
        INSTANCE.get().onEnable();
    }

    @ServiceInject
    static void stop() {
        INSTANCE.get().onDisable();
    }


    static NBTTagCompound getEntry(String id, String moduleId) {
        return INSTANCE.get().getDataEntry(id, moduleId);
    }

    static void save(String id) {
        INSTANCE.get().saveData(id);
    }

    static PlayerDataService create(String folder) {
        return new ServiceImplementation(new File(folder));
    }

    static int getEntryCount() {
        return INSTANCE.get().entryCount();
    }


    int entryCount();

    NBTTagCompound getDataEntry(String id, String moduleId);

    void saveData(String id);

    DataService getBackend();

    final class ServiceImplementation implements PlayerDataService, Listener {
        private final DataService backend;

        public ServiceImplementation(File f) {
            this.backend = new DataService(Quark.LOGGER, f);
        }

        @Override
        public void onEnable() {
            DataFix.moveFolder("/player_data", "/data/player");
            BukkitUtil.registerEventListener(this);
            this.backend.open();
        }

        @Override
        public void onDisable() {
            BukkitUtil.unregisterEventListener(this);
            this.backend.close();
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            this.backend.getEntry(event.getPlayer().getName());
        }

        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent event) {
            this.backend.saveEntry(event.getPlayer().getName());
        }

        @Override
        public int entryCount() {
            return this.backend.getEntryCount();
        }

        @Override
        public NBTTagCompound getDataEntry(String id, String namespace) {
            NBTTagCompound root = this.backend.getEntry(id);
            if (root.hasKey(namespace)) {
                return root.getCompoundTag(namespace);
            }
            NBTTagCompound tag = new NBTTagCompound();
            root.setCompoundTag(namespace, tag);
            saveData(id);
            return tag;
        }

        @Override
        public void saveData(String id) {
            this.backend.saveEntry(id);
        }

        @Override
        public DataService getBackend() {
            return this.backend;
        }
    }
}