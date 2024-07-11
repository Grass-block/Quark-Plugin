package org.tbstcraft.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.api.QueryPingEvent;
import org.tbstcraft.quark.data.assets.Asset;
import org.tbstcraft.quark.foundation.command.CommandExecuter;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.text.ComponentBlock;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@QuarkModule(version = "1.0.2")
@CommandProvider({CustomMotd.MotdCommand.class})
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CustomMotd extends PackageModule implements CommandExecuter {
    public static final Pattern PATTERN = Pattern.compile("\\{[a-z]+}");

    private CachedServerIcon cachedServerIcon;
    private YamlConfiguration setting;

    @Inject("motd.png;false")
    private Asset motdIcon;

    @Inject("motd.yml;false")
    private Asset motdText;

    @Override
    public void enable() {
        this.motdIcon.getFile();
        this.motdText.getFile();

        this.refreshIcon();
        this.refreshText();
    }

    private void refreshText() {
        this.motdText.asInputStream((s) -> {
            try {
                this.setting = YamlConfiguration.loadConfiguration(new InputStreamReader(s));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void refreshIcon() {
        try {
            this.cachedServerIcon = Bukkit.loadServerIcon(this.motdIcon.getFile());
        } catch (Exception e) {
            this.getLogger().severe("failed to load server icon. please consider refresh icon when fixed.");
        }
    }

    public ComponentBlock getMessage() {
        ConfigurationSection root = this.setting.getConfigurationSection("motd");

        if (root == null) {
            throw new RuntimeException("invalid config!");
        }

        String template = root.getString("motd-title") + "\n{#reset}" + root.getString("motd-subtitle");

        Matcher matcher = PATTERN.matcher(template);

        while (matcher.find()) {
            String raw = matcher.group();
            String key = raw.replace("{", "").replace("}", "");

            String content;

            if (!root.contains(key)) {
                content = key;
            } else if (root.isString(key)) {
                content = root.getString(key);
            } else {
                List<String> list = root.getStringList(key);
                content = list.get(SharedObjects.RANDOM.nextInt(list.size()));
            }

            if (content == null) {
                content = key;
            }

            template = template.replace(raw, content);
        }

        return TextBuilder.build(template);
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        e.setMotd(getMessage().toString());

        if (this.cachedServerIcon == null) {
            return;
        }
        e.setServerIcon(this.cachedServerIcon);
    }

    @EventHandler
    public void onPing(QueryPingEvent e) {
        e.setMotd(getMessage().toPlainTextString());

        if (this.cachedServerIcon == null) {
            return;
        }
        e.setServerIcon(this.cachedServerIcon);
    }


    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "refresh-icon" -> {
                refreshIcon();
                this.getLanguage().sendMessage(sender, "icon-refresh");
            }
            case "refresh-text" -> {
                this.refreshText();
                this.getLanguage().sendMessage(sender, "text-refresh");
            }
            case "text" -> {
                this.getLanguage().sendMessage(sender, "motd-command");
                getMessage().send(sender);
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("refresh-icon");
            tabList.add("refresh-text");
            tabList.add("text");
        }
    }

    @QuarkCommand(name = "motd", permission = "-quark.motd.command")
    public static final class MotdCommand extends ModuleCommand<CustomMotd> {
        @Override
        public void init(CustomMotd module) {
            this.setExecutor(module);
        }
    }
}