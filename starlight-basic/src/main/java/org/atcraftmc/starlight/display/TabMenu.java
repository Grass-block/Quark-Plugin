package org.atcraftmc.starlight.display;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO2;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(version = "2.0.3")
public final class TabMenu extends PackageModule {
    public static final String UPDATE_TASK_TID = "sl-display:tab-menu:update";

    @SuppressWarnings("Convert2MethodRef")
    MethodHandleO2<Player, Component, Component> SET_TAB = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Class.forName("com.comphenix.protocol.ProtocolLibrary").getMethod("getProtocolManager"), (p, h, f) -> {
            ProtocolLibHandler.sendTabList(p, h, f);
        });
        ctx.attempt(() -> Player.class.getMethod("sendPlayerListHeader", Component.class), (p, h, f) -> {
            p.sendPlayerListHeader(h);
            p.sendPlayerListFooter(f);
        });
        ctx.attempt(() -> Player.class.getMethod("setPlayerListHeaderFooter", BaseComponent.class, BaseComponent.class), (p, h, f) -> {
            var bh = ComponentSerializer.bungee(h);
            var bf = ComponentSerializer.bungee(f);
            p.setPlayerListHeaderFooter(bh, bf);
        });
        ctx.dummy((p, h, f) -> {
            p.setPlayerListHeader(ComponentSerializer.legacy(h));
            p.setPlayerListFooter(ComponentSerializer.legacy(f));
        });
    });


    @Override
    public void enable() {
        TaskService.async().timer(UPDATE_TASK_TID, 0, 20, this::update);
        BukkitUtil.registerEventListener(this);
        this.update();
    }

    @Override
    public void disable() {
        TaskService.async().cancel(UPDATE_TASK_TID);
        for (Player p : Bukkit.getOnlinePlayers()) {
            clearTab(p);
        }
    }

    public void clearTab(Player player) {
        Players.setPlayerTab(player, "", "");
        VisualScoreboardService.instance().visualScoreboard(player).clearTabColumn();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.setPlayerList(event.getPlayer());
    }

    private void update() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            setPlayerList(p);
        }
    }

    public void setPlayerList(Player player) {
        var locale = LocaleService.locale(player);
        var lang = Starlight.lang();

        var header = lang.inline(Language.generateTemplate(this.getConfig(), "header-ui"), locale, "starlight-display:tab-menu");
        var footer = lang.inline(Language.generateTemplate(this.getConfig(), "footer-ui"), locale, "starlight-display:tab-menu");

        header = PlaceHolderService.formatPlayer(player, header);
        footer = PlaceHolderService.formatPlayer(player, footer);

        if (getConfig().value("render-ping").bool()) {
            for (var p : Bukkit.getOnlinePlayers()) {
                var ping = Integer.parseInt(PlaceHolderService.PLAYER.get("ping-value", p));
                VisualScoreboardService.instance().visualScoreboard(player).setTabColumn(p, ping, Component.text("ms"));
            }
        }

        var h = TextBuilder.buildComponent(header);
        var f = TextBuilder.buildComponent(footer);

        SET_TAB.invoke(player, h, f);
    }

    public interface ProtocolLibHandler {
        static void sendTabList(Player player, Component header, Component footer) {
            var packet = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
            var headerComponent = WrappedChatComponent.fromJson(ComponentSerializer.json(header));
            var footerComponent = WrappedChatComponent.fromJson(ComponentSerializer.json(footer));

            packet.getChatComponents().write(0, headerComponent);
            packet.getChatComponents().write(1, footerComponent);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
