package org.atcraftmc.quark.web.account;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.tbstcraft.quark.data.PlayerDataService;

import java.util.HashMap;
import java.util.UUID;

interface AccountManager {
    HashMap<String, Runnable> CALLBACK = new HashMap<>(32);

    String ENTRY = "account";
    String MAIL = "mail";
    String STATUS = "status";

    static boolean verifyMail(String code) {
        if (!CALLBACK.containsKey(code)) {
            return false;
        }
        CALLBACK.get(code).run();
        CALLBACK.remove(code);
        return true;
    }

    static String generateActivationLink(String prefix, Runnable callback) {
        UUID randomUUID = UUID.randomUUID();
        String code = randomUUID.toString().replaceAll("-", "");
        CALLBACK.put(code, callback);
        return prefix + "/account/verify?code=" + code;
    }

    static NBTTagCompound getEntry(String player) {
        return PlayerDataService.getEntry(player, ENTRY);
    }

    static String getMail(String player) {
        return getEntry(player).getString(MAIL);
    }

    static boolean isValidMail(String player) {
        String mail = getMail(player);
        return mail != null && !mail.isEmpty() && !mail.isBlank();
    }

    static void setMail(String player, String mail) {
        if (mail == null) {
            getEntry(player).remove(MAIL);
            setStatus(player, AccountStatus.UNLINKED);
        } else {
            getEntry(player).setString(MAIL, mail);
        }
        PlayerDataService.save(player);
    }

    static AccountStatus getStatus(String player) {
        NBTTagCompound entry = getEntry(player);
        if (entry.hasKey(STATUS)) {
            return AccountStatus.fromId(entry.getString(STATUS));
        }
        entry.setString(STATUS, AccountStatus.UNLINKED.getId());
        PlayerDataService.save(player);
        return AccountStatus.UNLINKED;
    }

    static void setStatus(String player, AccountStatus status) {
        NBTTagCompound entry = getEntry(player);
        entry.setString(STATUS, status.getId());
        PlayerDataService.save(player);
    }
}
