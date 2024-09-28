package org.tbstcraft.quark.data;

import me.gb2022.commons.nbt.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.storage.DataEntry;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.util.DataFix;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;

@QuarkService(id = "player-data")
public interface PlayerDataService extends IDataService {

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
        return new Impl(new File(folder));
    }

    static int entryCount() {
        return INSTANCE.get().getEntryCount();
    }

    static DataEntry get(Player player) {
        return INSTANCE.get().getData(player.getName());
    }

    static DataEntry get(String player) {
        return INSTANCE.get().getData(player);
    }

    static void save(Player player) {
        INSTANCE.get().saveData(player.getName());
    }


    NBTTagCompound getDataEntry(String id, String moduleId);

    DataEntry getData(Player player);

    void saveData(Player player);


    final class Impl extends DataService implements PlayerDataService {
        private final Listener listener = new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event) {
                Impl.this.getEntry(event.getPlayer().getName());
            }

            @EventHandler
            public void onPlayerLeave(PlayerQuitEvent event) {
                Impl.this.saveEntry(event.getPlayer().getName());
            }
        };

        public Impl(File f) {
            super(Quark.getInstance().getLogger(), f);
        }

        @Override
        public DataEntry getData(Player player) {
            return getData(player.getName());
        }

        @Override
        public void saveData(Player player) {
            saveData(player.getName());
        }

        @Override
        public DataEntry getData(String player) {
            return this.get(player);
        }

        @Override
        public void saveData(String player) {
            this.saveEntry(player);
        }

        @Override
        public void onEnable() {
            DataFix.moveFolder("/player_data", "/data/player");
            BukkitUtil.registerEventListener(this.listener);
            this.open();
        }

        @Override
        public void onDisable() {
            BukkitUtil.unregisterEventListener(this.listener);
            this.close();
        }

        @Override
        public NBTTagCompound getDataEntry(String id, String namespace) {
            var entry = get(id);

            if (!entry.hasKey(namespace)) {
                entry.setCompoundTag(namespace, new NBTTagCompound());
            }

            return entry.getTable(namespace);
        }
    }
}