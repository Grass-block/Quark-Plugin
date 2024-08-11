package org.atcraftmc.quark.web;

import me.gb2022.commons.math.SHA;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.framework.service.Service;
import me.gb2022.commons.container.ObjectContainer;

import java.util.Base64;
import java.util.Objects;
import java.util.Random;

public interface PlayerAuthService extends Service {
    String DATA_SECTION_ID = "auth_service";
    String PATH = "auth:/encrypted";
    ObjectContainer<PlayerAuthService> INSTANCE = new ObjectContainer<>();

    static void init() {
        INSTANCE.set(create(PlayerDataService.INSTANCE.get()));
        INSTANCE.get().onEnable();
    }

    static void stop() {
        INSTANCE.get().onDisable();
    }

    static boolean verify(String name, String password) {
        return INSTANCE.get().verifyPlayer(name, password);
    }

    static void set(String name, String password) {
        INSTANCE.get().setPlayerPassword(name, password);
    }

    static PlayerAuthService create(PlayerDataService dataSupport) {
        return new ServiceImplementation(dataSupport);
    }

    static String generateRandom() {
        byte[] b = new byte[32];
        new Random().nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }

    void setPlayerPassword(String player, String password);

    boolean verifyPlayer(String player, String password);


    final class ServiceImplementation implements PlayerAuthService, Listener {
        private final PlayerDataService dataSupport;

        public ServiceImplementation(PlayerDataService dataSupport) {
            this.dataSupport = dataSupport;
        }

        @Override
        public void onEnable() {
            BukkitUtil.registerEventListener(this);
        }

        @Override
        public void onDisable() {
            BukkitUtil.unregisterEventListener(this);
        }

        @Override
        public void setPlayerPassword(String player, String password) {
            NBTTagCompound tag = this.dataSupport.getDataEntry(player, DATA_SECTION_ID);
            if (tag.hasKey(PATH)) {
                return;
            }
            tag.setString(PATH, SHA.getSHA512(password, false));
            this.dataSupport.saveData(player);
        }

        @Override
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean verifyPlayer(String player, String password) {
            NBTTagCompound tag = PlayerDataService.getEntry(player, "auth_service");

            if (!tag.hasKey(PATH)) {
                return false;
            }

            if (Bukkit.getPlayerExact(player) == null) {
                return false;
            }
            return Objects.equals(SHA.getSHA512(password, false), tag.getString(PATH));
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            NBTTagCompound tag = PlayerDataService.getEntry(event.getPlayer().getName(), "auth_service");
            if (tag.hasKey(PATH)) {
                return;
            }
            String pwd = generateRandom();
            set(event.getPlayer().getName(), pwd);
            //Quark.LANGUAGE.sendMessage(event.getPlayer(), "auth", "password_set", pwd);
        }
    }
}
