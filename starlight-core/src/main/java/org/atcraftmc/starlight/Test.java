package org.atcraftmc.starlight;

import me.gb2022.commons.math.SHA;
import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.atcraftmc.starlight.util.Identifiers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

public interface Test {
    String[] KEYS = {
            "atc-activities:potato-war",
            "atc-addon:ac-potato-war",
            "atc-addon:ac-realistic-survival",
            "atc-addon:guns-dimension-liberation",
            "atc-addons:ac-potato-war",
            "atc-addons:guns-dimension-liberation",
            "quark-automatic:auto-plugin-reload",
            "quark-automatic:auto-save",
            "quark-automatic:vm-garbage-cleaner",
            "quark-chat:chat-at",
            "quark-chat:chat-component",
            "quark-chat:chat-filter",
            "quark-chat:chat-mute",
            "quark-chat:chat-report",
            "quark-chat:chat-translator",
            "quark-chat:chatgpt",
            "quark-chat:daily-sentence",
            "quark-chat:hitokoto",
            "quark-chat:mail",
            "quark-chat:npc-chat",
            "quark-chat:qq-chat-sync",
            "quark-chat:self-message",
            "quark-clientsupport:player-protocol-display",
            "quark-commands:command-exec",
            "quark-commands:command-function",
            "quark-commands:command-variables",
            "quark-commands:console-command",
            "quark-commands:entity-motion",
            "quark-commands:item-command",
            "quark-commands:position-align",
            "quark-commands:self-message",
            "quark-contents:114514",
            "quark-contents:custom-recipe",
            "quark-contents:elevator",
            "quark-contents:elytra-aeronautics",
            "quark-contents:hats",
            "quark-contents:midi-node-block-player",
            "quark-contents:minecart-controller",
            "quark-contents:music-player",
            "quark-contents:neko",
            "quark-contents:realistic-minecart",
            "quark-contents:realistic-sleep",
            "quark-contents:sit-on-player",
            "quark-contents:stair-seat",
            "quark-contents:tpa",
            "quark-contents:waypoint",
            "quark-core:advertisements",
            "quark-core:counter-conflict-handler",
            "quark-core:custom-language-pack-loader",
            "quark-core:demo-warning",
            "quark-core:demo_warning",
            "quark-core:fixes",
            "quark-core:incomplete-installation-detector",
            "quark-core:installation-check",
            "quark-core:mcdr-chat-forwarding",
            "quark-core:modrinth-update-check",
            "quark-core:modrinth-version-check",
            "quark-core:null",
            "quark-core:platform-patcher",
            "quark-core:reserved",
            "quark-core:test",
            "quark-core:version-log-viewer",
            "quark-display:action-bar-hud",
            "quark-display:afk",
            "quark-display:bossbar-announcement",
            "quark-display:chat-announce",
            "quark-display:chat-format",
            "quark-display:custom-ban-message",
            "quark-display:custom-death-message",
            "quark-display:custom-kick-message",
            "quark-display:custom-motd",
            "quark-display:custom-scoreboard",
            "quark-display:drop-item-info",
            "quark-display:hover-display",
            "quark-display:join-quit-message",
            "quark-display:nickname",
            "quark-display:player-name-header",
            "quark-display:player-skin-customizer",
            "quark-display:tab-menu",
            "quark-display:we-selection-renderer",
            "quark-display:we-session-renderer",
            "quark-display:welcome-message",
            "quark-lobby:back-to-spawn",
            "quark-lobby:default-inventory",
            "quark-lobby:map-protect",
            "quark-lobby:npc-chat",
            "quark-lobby:player-protect",
            "quark-lobby:server-transfer-message",
            "quark-management:advanced-ban-command",
            "quark-management:advanced-plugin-command",
            "quark-management:clear-console-command",
            "quark-management:kick-on-reload",
            "quark-management:maintenance",
            "quark-management:server-info",
            "quark-management:stop-confirm",
            "quark-management:system-util",
            "quark-management:tps-bar",
            "quark-management:tpsbar",
            "quark-proxysupport:bungee-connection-protect",
            "quark-proxysupport:chat-sync",
            "quark-proxysupport:forge-server-teleportation",
            "quark-proxysupport:geyser-skin-redirect",
            "quark-proxysupport:legacy-forwarding-protect",
            "quark-proxysupport:mcsm-dynamic-instance",
            "quark-proxysupport:proxy-ping",
            "quark-proxysupport:proxy-transfer-display",
            "quark-proxysupport:server-connect",
            "quark-proxysupport:server-statement-observer",
            "quark-security:account-activation",
            "quark-security:advanced-permission-control",
            "quark-security:explosion-defender",
            "quark-security:inventory-menu",
            "quark-security:ip-defender",
            "quark-security:item-defender",
            "quark-security:permission-manager",
            "quark-security:protection-area",
            "quark-security:we-session-size-limit",
            "quark-security:we-size-defender",
            "quark-storage:item-drop-secure",
            "quark-storage:portable-functional-blocks",
            "quark-storage:portable-shulker-box",
            "quark-tweaks:crop-click-harvest",
            "quark-tweaks:dispenser-block-placer",
            "quark-tweaks:double-door-sync",
            "quark-tweaks:entity-leash",
            "quark-tweaks:fly-speed-modifier",
            "quark-tweaks:freecam",
            "quark-tweaks:portable-shulker-box",
            "quark-tweaks:realistic-sleep",
            "quark-tweaks:time-scale",
            "quark-tweaks:vanilla-tweaks",
            "quark-tweaks:vein-miner",
            "quark-utilities:advanced-ban-command",
            "quark-utilities:advanced-plugin-command",
            "quark-utilities:block-update-locker",
            "quark-utilities:calculator",
            "quark-utilities:camera-movement",
            "quark-utilities:command-exec",
            "quark-utilities:command-function",
            "quark-utilities:command-tab-fix",
            "quark-utilities:console-command",
            "quark-utilities:custom-log-format",
            "quark-utilities:dynamic-view-distance",
            "quark-utilities:entity-motion",
            "quark-utilities:force-sprint",
            "quark-utilities:inventory-menu",
            "quark-utilities:item-command",
            "quark-utilities:item-custom-name",
            "quark-utilities:kick-on-reload",
            "quark-utilities:log-color-patch",
            "quark-utilities:maintenance",
            "quark-utilities:player-ping-command",
            "quark-utilities:player-position-lock",
            "quark-utilities:position-align",
            "quark-utilities:server-freeze",
            "quark-utilities:server-idle",
            "quark-utilities:stop-confirm",
            "quark-utilities:surrounding-refresh",
            "quark-utilities:unexpected-kick-prevent",
            "quark-utilities:worldedit-commands",
            "quark-warps:back-to-death",
            "quark-warps:rtp",
            "quark-warps:tpa",
            "quark-warps:waypoint",
            "quark-web-auth:minecraft-sso-authorization",
            "quark-web:account-activation",
            "quark-web:ip-defender",
            "quark-web:server-queries"
    };

    static void main2(String[] args) {
        for (var s : new String[]{"4e843650-e7fc-39e8-b729-230907616d88"}) {
            var sha = SHA.getSHA1(Identifiers.internal(s), false);

            //System.out.println(s + " -> " + sha); //+ "/" + SHA.getSHA1(Identifiers.internal(s.split(":")[1]), false));

            var d = new ByteArrayOutputStream();

            NBT.writeZipped(new NBTTagCompound(), d);


            System.out.println(Base64.getEncoder().encodeToString(d.toByteArray()));
        }
    }

    static void main(String[] args) {
        for (int i = 0; i < 10000; i++) {
            codec();
        }

        var last = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            codec();
        }

        System.out.println((System.currentTimeMillis() - last) / 100000F);
    }


    static void object(NBTTagCompound t) {
        t.setDouble(UUID.randomUUID().toString(), Math.random());
        t.setDouble(UUID.randomUUID().toString(), Math.random());
        t.setDouble(UUID.randomUUID().toString(), Math.random());
        t.setString(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        t.setString(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        t.setString(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    static void codec() {
        var n = new NBTTagCompound();

        object(n);

        var bb = new ByteArrayOutputStream();

        NBT.write(n, bb);

        var b = Base64.getEncoder().encodeToString(bb.toByteArray());

        var bbb = new ByteArrayInputStream(Base64.getDecoder().decode(b));

        NBT.read(bbb);
    }
}
