package org.atcraftmc.quark.display;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.*;

@QuarkModule(version = "0.3.0")
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
        int p = this.getConfig().getInt("period");
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
        return this.language.getMessageList(Language.locale(sender), "content");
    }

    private void sendHint(CommandSender sender) {
        Locale locale = Language.locale(sender);
        String msg = this.getContents(sender).get((int) (this.index % this.getContents(sender).size()));
        String mode = this.language.getMessage(locale, "type-hint");

        this.language.sendTemplate(sender, Language.generateTemplate(this.getConfig(), "ui", (s) -> s.formatted(mode, msg)));
    }

    private void sendAnnounce(CommandSender sender) {
        NBTTagCompound tag = ModuleDataService.getEntry(this.getFullId());

        if (!tag.hasKey("announce")) {
            return;
        }

        Locale locale = Language.locale(sender);
        String msg = tag.getString("announce");
        String mode = this.language.getMessage(locale, "type-announce");

        this.language.sendTemplate(sender, Language.generateTemplate(this.getConfig(), "ui", (s) -> s.formatted(mode, msg)));
    }

    private void sendTip(CommandSender sender) {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> {
            List<LanguageItem> list = new ArrayList<>();

            for (Object o : s) {
                list.add((LanguageItem) o);
            }

            Locale locale = Language.locale(sender);

            String msg = list.get(new Random().nextInt(list.size())).getMessage(Language.locale(sender));
            String mode = this.language.getMessage(locale, "type-tip");
            String btn = this.language.getMessage(locale, "tip-append");

            this.language.sendTemplate(sender,
                                       Language.generateTemplate(this.getConfig(), "ui", (ss) -> ss.formatted(mode + "  " + btn, msg))
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
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "hint" -> {
                    this.getModule().sendHint(sender);
                    this.getModule().index++;
                }
                case "tips" -> this.getModule().sendTip(sender);
                case "announce" -> this.getModule().sendAnnounce(sender);
                case "set-announce" -> {
                    if (!sender.isOp()) {
                        this.sendPermissionMessage(sender, "(ServerOperator)");
                        return;
                    }

                    String content = Objects.equals(args[1], "none") ? null : args[1] + "{;}";

                    String entry = this.getModuleFullId();
                    if (content == null) {
                        ModuleDataService.getEntry(entry).getTagMap().remove("announce");
                    } else {
                        ModuleDataService.getEntry(entry).setString("announce", content);
                    }

                    ModuleDataService.save(entry);

                    if (content == null) {
                        this.getLanguage().sendMessage(sender, "custom-clear");
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p == sender) {
                                continue;
                            }
                            this.getModule().sendAnnounce(sender);
                        }
                        this.getLanguage().sendMessage(sender, "custom-set", content);
                    }
                }
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("hint");
                tabList.add("tips");
                tabList.add("set-announce");
                tabList.add("announce");
            }
        }
    }
}
