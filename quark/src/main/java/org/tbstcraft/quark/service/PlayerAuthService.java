package org.tbstcraft.quark.service;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

import java.util.Base64;
import java.util.Objects;
import java.util.Random;

public interface PlayerAuthService {
    String PATH = "auth:/encrypted";
    EventHolder EVENT_HOLDER = new EventHolder();

    static void init() {
        Bukkit.getPluginManager().registerEvents(EVENT_HOLDER, Quark.PLUGIN);
    }

    static void stop() {
        PlayerJoinEvent.getHandlerList().unregister(EVENT_HOLDER);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean verify(String name, String password) {
        NBTTagCompound tag = PlayerDataService.getEntry(name);

        if (!tag.hasKey(PATH)) {
            return false;
        }

        if (Bukkit.getPlayer(name) == null) {
            return false;
        }
        return Objects.equals(BukkitUtil.encrypt(password), tag.getString(PATH));
    }

    static void set(String name, String password) {
        NBTTagCompound tag = PlayerDataService.getEntry(name);
        if (tag.hasKey(PATH)) {
            return;
        }
        tag.setString(PATH, BukkitUtil.encrypt(password));
        PlayerDataService.save(name);
    }

    static String generateRandom() {
        byte[] b = new byte[32];
        new Random().nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }

    class EventHolder implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            NBTTagCompound tag = PlayerDataService.getEntry(event.getPlayer().getName());
            if (tag.hasKey(PATH)) {
                return;
            }
            String pwd = generateRandom();
            set(event.getPlayer().getName(), pwd);
            Quark.LANGUAGE.sendMessageTo(event.getPlayer(), "auth", "password_set", pwd);
        }
    }
}
