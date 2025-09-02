package org.atcraftmc.starlight.core;

import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleRO0;
import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.language.LocaleMapping;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.qlib.texts.ComponentBlock;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.api.event.ClientLocaleChangeEvent;
import org.atcraftmc.starlight.core.data.flex.TableColumn;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CoreCommand;
import org.atcraftmc.starlight.foundation.command.StarlightCommandManager;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.framework.service.ServiceInject;
import org.atcraftmc.starlight.framework.service.ServiceLayer;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.plugin.ServicePriority;

import java.sql.SQLException;
import java.util.*;

@SLService(id = "locale", layer = ServiceLayer.FRAMEWORK)
public interface LocaleService extends Service {
    TableColumn<String> TESTED_LOCALE = TableColumn.string("lang_tested", 16, "unknown");
    TableColumn<String> CUSTOM_LOCALE = TableColumn.string("lang_custom", 16, "auto");

    Map<UUID, String> LOCALE_CACHE = new HashMap<>();

    @SuppressWarnings("Convert2MethodRef")
    MethodHandleRO0<Player, String> GET_LOCALE = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("getLocale"), (p) -> p.getLocale());
        ctx.dummy((p) -> LocaleMapping.minecraft(Locale.getDefault()));
    });
    AbstractCommand LANGUAGE_COMMAND = new LanguageDecideCommand();
    Listener LISTENER = new BukkitListener();

    @ServiceInject
    static void start() {
        try {
            PlayerDataService.PLAYER_LOCAL.init(JDBCService.getDB(JDBCService.SL_LOCAL).orElseThrow());
            PlayerDataService.PLAYER_SHARED.init(JDBCService.getDB(JDBCService.SL_SHARED).orElseThrow());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        StarlightCommandManager.getInstance().register(LANGUAGE_COMMAND);
        BukkitUtil.registerEventListener(LISTENER);

        Bukkit.getServicesManager().register(BukkitAdapter.class, new BukkitAdapter(), Starlight.instance(), ServicePriority.High);
    }

    @ServiceInject
    static void stop() {
        StarlightCommandManager.getInstance().unregister(LANGUAGE_COMMAND);
        BukkitUtil.unregisterEventListener(LISTENER);

        Bukkit.getServicesManager().unregister(BukkitAdapter.class);
    }

    @SuppressWarnings("deprecation")//because other server software still uses it.
    static MinecraftLocale locale(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender) {
            return MinecraftLocale.locale(Locale.getDefault());
        }
        if (!(sender instanceof Player p)) {
            return MinecraftLocale.locale(Locale.getDefault());
        }

        if (LOCALE_CACHE.containsKey(p.getUniqueId())) {
            return MinecraftLocale.minecraft(LOCALE_CACHE.get(p.getUniqueId()));
        }

        var locale = getUserLocale(p);
        LOCALE_CACHE.put(p.getUniqueId(), locale);
        return MinecraftLocale.minecraft(locale);
    }

    static void setCustomLanguage(Player user, String value) {
        CUSTOM_LOCALE.set(PlayerDataService.PLAYER_SHARED, user.getUniqueId(), value);
        LOCALE_CACHE.put(user.getUniqueId(), getUserLocale(user));
    }

    static String getUserLocale(Player user) {
        try {
            var custom = CUSTOM_LOCALE.get(PlayerDataService.PLAYER_SHARED, user.getUniqueId());

            if (!Objects.equals(custom, "auto")) {
                return custom;
            }

            var tested = TESTED_LOCALE.get(PlayerDataService.PLAYER_SHARED, user.getUniqueId());

            if (!Objects.equals(tested, "unknown")) {
                return tested;
            }
        } catch (Exception ignored) {
        }

        return saveGetMCPlayerLocale(user);
    }

    static String saveGetMCPlayerLocale(Player player) {
        return GET_LOCALE.invoke(player);
    }

    final class BukkitListener implements Listener {

        @EventHandler
        public void onLocaleChange(PlayerLocaleChangeEvent event) {
            _check(event);
            TaskService.global()
                    .delay(60, () -> _check(new PlayerLocaleChangeEvent(event.getPlayer(), saveGetMCPlayerLocale(event.getPlayer()))));
        }

        private void _check(PlayerLocaleChangeEvent event) {
            var preset = Starlight.instance().language().item("starlight-core.locale.preset");
            var locale = "zh_cn";
            try {
                locale = saveGetMCPlayerLocale(event.getPlayer());
            } catch (Exception e) {
                locale = LocaleMapping.minecraft(Locale.getDefault());
            }

            boolean isValidChange = true;

            var uuid = event.getPlayer().getUniqueId();
            var custom = CUSTOM_LOCALE.get(PlayerDataService.PLAYER_SHARED, uuid);
            var cache = TESTED_LOCALE.get(PlayerDataService.PLAYER_SHARED, uuid);

            if (Objects.equals(locale, "en_us")) {
                if (!Objects.equals(cache, "unknown")) {
                    locale = cache;
                    isValidChange = false;
                }

                if (!Objects.equals(custom, "auto")) {
                    locale = custom;
                    isValidChange = false;
                }
            }

            if (Objects.equals(custom, "auto")) {
                TESTED_LOCALE.set(PlayerDataService.PLAYER_SHARED, uuid, locale);
                if (isValidChange) {
                    ComponentBlock block = preset.component(locale(event.getPlayer()), locale);
                    TextSender.sendBlock(event.getPlayer(), block);
                    LOCALE_CACHE.put(uuid, locale);
                }
            } else {
                if (!Objects.equals(cache, "unknown")) {
                    locale = cache;
                }
            }

            var loc = MinecraftLocale.minecraft(locale);
            BukkitUtil.callEvent(new ClientLocaleChangeEvent(event.getPlayer(), loc));
        }
    }

    final class BukkitAdapter {
        public void setCustomLanguage(Player user, String value) {
            LocaleService.setCustomLanguage(user, value);
        }

        public String getUserLocale(Player user) {
            return LocaleService.getUserLocale(user);
        }

        public MinecraftLocale locale(CommandSender sender) {
            return LocaleService.locale(sender);
        }
    }

    @SuppressWarnings("deprecation")//because other server still uses getLocale()
    @QuarkCommand(name = "locale", permission = "+quark.locale", playerOnly = true)
    final class LanguageDecideCommand extends CoreCommand {

        @Override
        public void execute(CommandExecution context) {
            var data = context.requireArgumentAt(0);
            if (Objects.equals(data, "auto")) {
                data = saveGetMCPlayerLocale(context.requireSenderAsPlayer());
            }

            setCustomLanguage(context.requireSenderAsPlayer(), data);
            Starlight.lang().item("starlight-core.locale.set").send(context.getSender(), data);
            BukkitUtil.callEvent(new ClientLocaleChangeEvent((Player) context.getSender(), locale(context.getSender())));
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.addAll(List.of(LocaleMapping.MINECRAFT_KNOWN_LANGUAGES));
                tabList.add("auto");
                tabList.add("none");
            }
        }
    }
}
