package org.tbstcraft.quark.internal;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
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
import org.tbstcraft.quark.data.language.LocaleMapping;
import org.tbstcraft.quark.foundation.command.AbstractCommand;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@QuarkService(id = "locale")
public interface LocaleService extends Service {
    AbstractCommand LANGUAGE_COMMAND = new LanguageDecideCommand();
    Listener LISTENER = new BukkitListener();

    @ServiceInject
    static void start() {
        CommandManager.registerQuarkCommand(LANGUAGE_COMMAND);
        BukkitUtil.registerEventListener(LISTENER);

        Bukkit.getServicesManager().register(BukkitAdapter.class, new BukkitAdapter(), Quark.PLUGIN, ServicePriority.High);
    }

    @ServiceInject
    static void stop() {
        CommandManager.unregister(LANGUAGE_COMMAND);
        BukkitUtil.unregisterEventListener(LISTENER);

        Bukkit.getServicesManager().unregister(BukkitAdapter.class);
    }

    @SuppressWarnings("deprecation")//because other server still uses it.
    static Locale locale(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return Locale.getDefault();
        }
        String locale = getUserLocale(((Player) sender));
        return LocaleMapping.locale(locale);
    }

    static void setCustomLanguage(String name, String value) {
        NBTTagCompound tag = PlayerDataService.getEntry(name, "locale");

        if (tag.getString("custom").equals(value)) {
            return;
        }

        tag.setString("custom", value);
        PlayerDataService.save(name);
    }

    static String getUserLocale(Player user) {
        NBTTagCompound tag = PlayerDataService.getEntry(user.getName(), "locale");

        if (tag.hasKey("custom") && !tag.getString("custom").equals("none")) {
            return tag.getString("custom");
        }

        if (tag.hasKey("cache")) {
            return tag.getString("cache");
        }

        return user.getLocale();
    }

    final class BukkitListener implements Listener {

        @EventHandler
        public void onLocaleChange(PlayerLocaleChangeEvent event) {
            _check(event);
            if (event.getLocale().equals("en_us")) {
                TaskService.laterTask(60, () -> _check(new PlayerLocaleChangeEvent(event.getPlayer(), event.getPlayer().getLocale())));
            }
        }

        private void _check(PlayerLocaleChangeEvent event) {
            var preset = Quark.LANGUAGE.item("locale", "preset");
            var tag = PlayerDataService.getEntry(event.getPlayer().getName(), "locale");
            var locale = event.getPlayer().getLocale();

            if (!tag.hasKey("custom") || tag.getString("custom").equals("none")) {

                tag.setString("custom", "none");
                tag.setString("cache", locale);
                TextSender.sendTo(event.getPlayer(), preset.getMessageComponent(locale(event.getPlayer()), locale));
            } else {
                locale = tag.getString("cache");
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
                args[0] = ((Player) sender).getLocale();
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
