package org.atcraftmc.quark.display;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.texts.ComponentBlock;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.api.event.QueryPingEvent;
import org.atcraftmc.starlight.data.assets.Asset;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.MessageAccessor;

import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SLModule(version = "1.0.2")
@CommandProvider({CustomMotd.MotdCommand.class})
@AutoRegister(ServiceType.EVENT_LISTEN)
@Components(CustomMotd.ProtocolLibSender.class)
public final class CustomMotd extends PackageModule implements PluginCommandExecutor {
    public static final Pattern PATTERN = Pattern.compile("\\{[a-z]+}");

    private CachedServerIcon cachedServerIcon;
    private YamlConfiguration setting;

    @Inject("motd.png;false")
    private Asset motdIcon;

    @Inject("motd.yml;false")
    private Asset motdText;

    @Inject
    private Logger logger;

    @Inject
    private LanguageEntry language;

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
            this.logger.error("failed to load server icon. please consider refresh icon when fixed.", e);
        }
    }

    public ComponentBlock getMessage() {
        ConfigurationSection root = this.setting.getConfigurationSection("motd");

        if (root == null) {
            throw new RuntimeException("invalid config!");
        }

        var template = root.getString("motd-title") + "\n{#reset}" + root.getString("motd-subtitle");

        Matcher matcher = PATTERN.matcher(template);

        while (matcher.find()) {
            var raw = matcher.group();
            var key = raw.replace("{", "").replace("}", "");

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
        try {
            e.motd(getMessage().toSingleLine());
        } catch (Error ex) {
            e.setMotd(getMessage().toString());
        }

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
                MessageAccessor.send(this.language, sender, "icon-refresh");
            }
            case "refresh-text" -> {
                this.refreshText();
                MessageAccessor.send(this.language, sender, "text-refresh");
            }
            case "text" -> {
                MessageAccessor.send(this.language, sender, "motd-command");
                TextSender.sendMessage(sender, getMessage());
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

    public static final class ProtocolLibSender extends ModuleComponent<CustomMotd> {
        private PacketAdapter handler;

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("com.comphenix.protocol.ProtocolLibrary"));
        }

        @Override
        public void enable() {
            this.handler = new PacketAdapter(this.parent.getOwnerPlugin(), PacketType.Status.Server.OUT_SERVER_INFO) {
                @Override
                public void onPacketSending(PacketEvent e) {
                    WrappedServerPing ping = e.getPacket().getServerPings().read(0);
                    ping.setMotD(WrappedChatComponent.fromJson(ComponentSerializer.json(parent.getMessage())));
                }
            };
            ProtocolLibrary.getProtocolManager().addPacketListener(this.handler);
        }

        @Override
        public void disable() {
            ProtocolLibrary.getProtocolManager().removePacketListener(this.handler);
        }
    }

}