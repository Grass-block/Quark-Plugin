package org.atcraftmc.quark.web_auth;

import me.gb2022.commons.math.SHA;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.atcraftmc.starlight.framework.SLService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import me.gb2022.commons.container.ObjectContainer;

import java.util.Base64;
import java.util.Objects;
import java.util.Random;

public interface PlayerAuthService extends SLService {
    String DATA_SECTION_ID = "auth_service";
    String PATH = "auth:/encrypted";
    ObjectContainer<PlayerAuthService> INSTANCE = new ObjectContainer<>();

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
        public void enable() {
            BukkitUtil.registerEventListener(this);
        }

        @Override
        public void disable() {
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
            //Starlight.LANGUAGE.sendMessage(event.getPlayer(), "auth", "password_set", pwd);
        }
    }
}
