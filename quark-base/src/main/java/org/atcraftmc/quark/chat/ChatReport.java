package org.atcraftmc.quark.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.math.SHA;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.atcraftmc.quark.CustomChatRenderer;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.event.CustomEvent;
import org.tbstcraft.quark.framework.event.QuarkEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.*;

@QuarkModule(version = "1.0.0", compatBlackList = {APIProfile.SPIGOT, APIProfile.BUKKIT, APIProfile.ARCLIGHT}, recordFormat = {"Time", "OperationID", "Reporter", "SendTime", "Sender", "Content"})
@CommandProvider(ChatReport.ChatReportCommand.class)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ChatReport extends PackageModule {
    private final Map<String, String> records = new HashMap<>();

    @Inject("tip")
    private LanguageItem tip;

    public static String hash(String s) {
        return SHA.getSHA1(s, false).substring(9);
    }

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        String sender = event.getPlayer().getName();
        String time = SharedObjects.DATE_FORMAT.format(new Date());
        String content = LegacyComponentSerializer.legacySection().serialize(event.message());

        String shorted = (content.substring(0, Math.min(content.length(), 4)) + "...").replace(" ", "");
        String uuid = hash(content);

        String template = Objects.requireNonNull(getConfig().getString("append")).formatted(uuid);
        CustomChatRenderer.renderer(event).postfixNearest(TextBuilder.buildComponent(template));
        this.records.put(uuid, "%s;%s;%s;%s".formatted(time, sender, content, shorted));
    }

    @QuarkCommand(name = "chat-report")
    public static final class ChatReportCommand extends ModuleCommand<ChatReport> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (!this.getModule().records.containsKey(args[0])) {
                this.getLanguage().sendMessage(sender, "not-exist");
                return;
            }

            String[] rec = this.getModule().records.get(args[0]).split(";");

            if(rec[1].equalsIgnoreCase(sender.getName())){
                this.getLanguage().sendMessage(sender, "not-self");
                return;
            }

            this.getModule().records.remove(args[0]);
            this.getLanguage().sendMessage(sender, "success", rec[1], rec[3]);
            this.getModule()
                    .getRecord()
                    .addLine(SharedObjects.DATE_FORMAT.format(new Date()), args[0], sender.getName(), rec[0], rec[1], rec[2]);

            BukkitUtil.callEvent(new ChatReportedEvent(rec[1], rec[2], rec[3], args[0]), (e) -> {
                if (e.getOutcome() == null) {
                    return;
                }
                e.getOutcome().sendMessage(sender, rec[1], rec[3]);
            });
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("<uuid>");
            }
        }
    }

    @QuarkEvent
    public static final class ChatReportedEvent extends CustomEvent {
        private final String sender;
        private final String content;
        private final String shorted;
        private final String uuid;

        private LanguageItem outcome;

        public ChatReportedEvent(String sender, String content, String shorted, String uuid) {
            this.sender = sender;
            this.content = content;
            this.shorted = shorted;
            this.uuid = uuid;
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(ChatReportedEvent.class);
        }

        public String getContent() {
            return content;
        }

        public String getSender() {
            return sender;
        }

        public String getShorted() {
            return shorted;
        }

        public String getUuid() {
            return uuid;
        }

        public LanguageItem getOutcome() {
            return outcome;
        }

        public void setOutcome(LanguageItem outcome) {
            this.outcome = outcome;
        }
    }
}

