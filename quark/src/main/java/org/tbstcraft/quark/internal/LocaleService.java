package org.tbstcraft.quark.internal;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleRO0;
import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.plugin.ServicePriority;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.api.ClientLocaleChangeEvent;
import org.tbstcraft.quark.data.PlayerDataService;
import org.atcraftmc.qlib.language.LocaleMapping;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommandManager;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.atcraftmc.qlib.texts.ComponentBlock;
import org.tbstcraft.quark.foundation.TextSender;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@QuarkService(id = "locale")
public interface LocaleService extends Service {
    @SuppressWarnings("Convert2MethodRef")
    MethodHandleRO0<Player, String> GET_LOCALE = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("getLocale"), (p) -> p.getLocale());
        ctx.dummy((p) -> LocaleMapping.minecraft(Locale.getDefault()));
    });
    AbstractCommand LANGUAGE_COMMAND = new LanguageDecideCommand();
    Listener LISTENER = new BukkitListener();

    @ServiceInject
    static void start() {
        QuarkCommandManager.getInstance().register(LANGUAGE_COMMAND);
        BukkitUtil.registerEventListener(LISTENER);

        Bukkit.getServicesManager().register(BukkitAdapter.class, new BukkitAdapter(), Quark.getInstance(), ServicePriority.High);
    }

    @ServiceInject
    static void stop() {
        QuarkCommandManager.getInstance().unregister(LANGUAGE_COMMAND);
        BukkitUtil.unregisterEventListener(LISTENER);

        Bukkit.getServicesManager().unregister(BukkitAdapter.class);
    }

    @SuppressWarnings("deprecation")//because other server still uses it.
    static Locale locale(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender) {
            return Locale.getDefault();
        }
        if(!(sender instanceof Player)){
            return Locale.getDefault();
        }
        String locale = getUserLocale(((Player) sender));
        return LocaleMapping.locale(locale);
    }

    static void setCustomLanguage(String name, String value) {
        NBTTagCompound tag = PlayerDataService.getEntry(name, "locale");

        if (tag.hasKey("custom") && tag.getString("custom").equals(value)) {
            return;
        }

        tag.setString("custom", value);
        PlayerDataService.save(name);
    }

    static String getUserLocale(Player user) {
        var entry = PlayerDataService.get(user).getTable("locale");

        if (entry.hasKey("custom") && !entry.getString("custom").equals("none")) {
            return entry.getString("custom");
        }

        if (entry.hasKey("cache")) {
            return entry.getString("cache");
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
            var preset = Quark.LANGUAGE.item("locale", "preset");
            var tag = PlayerDataService.getEntry(event.getPlayer().getName(), "locale");
            var locale = "zh_cn";
            try {
                locale = saveGetMCPlayerLocale(event.getPlayer());
            } catch (Exception e) {
                locale = LocaleMapping.minecraft(Locale.getDefault());
            }

            boolean isValidChange = true;

            if (Objects.equals(locale, "en_us")) {
                if (tag.hasKey("cache")) {
                    locale = tag.getString("cache");
                    isValidChange = false;
                }

                if (tag.hasKey("custom") && !tag.getString("custom").equals("none")) {
                    locale = tag.getString("custom");
                    isValidChange = false;
                }
            }

            if (!tag.hasKey("custom") || tag.getString("custom").equals("none")) {
                tag.setString("custom", "none");
                tag.setString("cache", locale);

                if (isValidChange) {
                    ComponentBlock block = preset.getMessageComponent(locale(event.getPlayer()), locale);
                    TextSender.sendBlock(event.getPlayer(), block);
                }
            } else {
                if (tag.hasKey("cache")) {
                    locale = tag.getString("cache");
                }
            }

            Locale loc = LocaleMapping.locale(locale);
            BukkitUtil.callEvent(new ClientLocaleChangeEvent(event.getPlayer(), loc));
        }
    }

    final class BukkitAdapter {
        public void setCustomLanguage(String name, String value) {
            LocaleService.setCustomLanguage(name, value);
        }

        public String getUserLocale(Player user) {
            return LocaleService.getUserLocale(user);
        }

        public Locale locale(CommandSender sender) {
            return LocaleService.locale(sender);
        }
    }

    @SuppressWarnings("deprecation")//because other server still uses getLocale()
    @QuarkCommand(name = "locale", permission = "+quark.locale", playerOnly = true)
    final class LanguageDecideCommand extends CoreCommand {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (Objects.equals(args[0], "auto")) {
                args[0] = saveGetMCPlayerLocale((Player) sender);
            }
            setCustomLanguage(sender.getName(), args[0]);
            Quark.LANGUAGE.sendMessage(sender, "locale", "set", args[0]);
            BukkitUtil.callEvent(new ClientLocaleChangeEvent(((Player) sender), locale(sender)));
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
