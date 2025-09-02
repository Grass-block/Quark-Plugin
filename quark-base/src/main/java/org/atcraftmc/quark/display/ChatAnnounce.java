package org.atcraftmc.quark.display;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.LanguageItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;

import java.util.*;

@SLModule(version = "0.3.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(ChatAnnounce.HintCommand.class)
public final class ChatAnnounce extends PackageModule {
    private Set<Player> sessions = new HashSet<>();

    private long index;
    private boolean freeze;

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() {
        this.sessions.addAll(Bukkit.getOnlinePlayers());

        this.tick();
        int p = ConfigAccessor.getInt(this.getConfig(), "period");
        TaskService.async().timer("chat-announce:tick", p, p, this::tick);
    }

    @Override
    public void disable() {
        this.sessions = null;
        TaskService.async().cancel("chat-announce:tick");
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {
        this.freeze = false;
    }


    public void tick() {
        if (this.sessions == null) {
            return;
        }
        if (this.freeze) {
            return;
        }
        this.index++;


        for (Player p : Bukkit.getOnlinePlayers()) {
            sendHint(p);
        }
        this.freeze = true;
    }

    private List<String> getContents(CommandSender sender) {
        return this.language.item("content").list(LocaleService.locale(sender));
    }

    private void sendHint(CommandSender sender) {
        var locale = LocaleService.locale(sender);
        var msg = this.getContents(sender).get((int) (this.index % this.getContents(sender).size()));
        var mode = MessageAccessor.getMessage(this.language, locale, "type-hint");

        MessageAccessor.sendTemplate(this.language, sender, Language.generateTemplate(this.getConfig(), "ui", (s) -> s.formatted(mode, msg)));
    }

    private void sendAnnounce(CommandSender sender) {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getFullId());

        if (!tag.hasKey("announce")) {
            return;
        }

        var locale = LocaleService.locale(sender);
        var msg = tag.getString("announce");
        var mode = MessageAccessor.getMessage(this.language, locale, "type-announce");

        MessageAccessor.sendTemplate(this.language, sender, Language.generateTemplate(this.getConfig(), "ui", (s) -> s.formatted(mode, msg)));
    }

    private void sendTip(CommandSender sender) {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> {
            var list = new ArrayList<LanguageItem>();

            for (Object o : s) {
                list.add((LanguageItem) o);
            }

            var locale = LocaleService.locale(sender);

            var msg = list.get(new Random().nextInt(list.size())).message(locale);
            var mode = MessageAccessor.getMessage(this.language, locale, "type-tip");
            var btn = MessageAccessor.getMessage(this.language, locale, "tip-append");

            MessageAccessor.sendTemplate(
                    this.language,
                    sender,
                    Language.generateTemplate(this.getConfig(), "ui", (ss) -> ss.formatted(mode + "  " + btn, msg))
            );
        });
    }

    private void sendCustomActivity(CommandSender sender) {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_CUSTOM_ACTIVITY, (s) -> {
            var list = new ArrayList<LanguageItem>();

            for (Object o : s) {
                list.add((LanguageItem) o);
            }

            if (list.isEmpty()) {
                return;
            }

            var locale = LocaleService.locale(sender);

            var msg = list.get(new Random().nextInt(list.size())).message(locale);

            MessageAccessor.sendTemplate(
                    this.language,
                    sender,
                    Language.generateTemplate(this.getConfig(), "ui", (ss) -> ss.formatted((Object[]) msg.split("::")))
            );
        });
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.sessions == null) {
            return;
        }
        this.sessions.add(event.getPlayer());
        this.sendTip(event.getPlayer());
        this.sendAnnounce(event.getPlayer());
        this.sendCustomActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (this.sessions == null) {
            return;
        }
        this.sessions.remove(event.getPlayer());
    }


    @QuarkCommand(name = "chat-hint")
    public static final class HintCommand extends ModuleCommand<ChatAnnounce> {
        @Override
        public void execute(CommandExecution context) {
            switch (context.requireEnum(0, "hint", "tips")) {
                case "hint" -> {
                    this.getModule().sendHint(context.getSender());
                    this.getModule().index++;
                }
                case "tips" -> this.getModule().sendTip(context.getSender());
            }
        }

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "hint", "tip");
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "announce" -> this.getModule().sendAnnounce(sender);
                case "set-announce" -> {
                    if (!sender.isOp()) {
                        this.sendPermissionMessage(sender, "(ServerOperator)");
                        return;
                    }

                    var content = Objects.equals(args[1], "none") ? null : args[1].replace(":blank:", " ") + "{;}";

                    var entry = this.getModuleFullId();
                    if (content == null) {
                        ModuleDataService.getEntry(entry).getTagMap().remove("announce");
                    } else {
                        ModuleDataService.getEntry(entry).setString("announce", content);
                    }

                    ModuleDataService.save(entry);

                    if (content == null) {
                        MessageAccessor.send(this.getLanguage(), sender, "custom-clear");
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p == sender) {
                                continue;
                            }
                            this.getModule().sendAnnounce(sender);
                        }
                        MessageAccessor.send(this.getLanguage(), sender, "custom-set", content);
                    }
                }
                case "view-activity" -> this.getModule().sendCustomActivity(sender);
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("hint");
                tabList.add("tips");
                tabList.add("set-announce");
                tabList.add("announce");
                tabList.add("view-activity");
            }
        }
    }
}
