package org.tbstcraft.quark.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.tbstcraft.quark.CustomChatRenderer;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.*;

@QuarkModule(
        version = "1.0.0",
        compatBlackList = {APIProfile.SPIGOT, APIProfile.BUKKIT, APIProfile.ARCLIGHT},
        recordFormat = {"Time", "OperationID", "Reporter", "SendTime", "Sender", "Content"}
)
@CommandProvider(ChatReport.ChatReportCommand.class)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ChatReport extends PackageModule {
    private final Map<String, String> records = new HashMap<>();

    @Inject("tip")
    private LanguageItem tip;

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
        String uuid = UUID.randomUUID().toString().split("-")[4];
        String sender = event.getPlayer().getName();
        String time = SharedObjects.DATE_FORMAT.format(new Date());
        String content = LegacyComponentSerializer.legacySection().serialize(event.message());

        String template = Objects.requireNonNull(getConfig().getString("append")).formatted(uuid);
        CustomChatRenderer.renderer(event).postfixNearest(TextBuilder.buildComponent(template));
        this.records.put(uuid, "%s;%s;%s".formatted(time, sender, content));
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
            this.getModule().records.remove(args[0]);
            this.getLanguage().sendMessage(sender, "success", rec[1], args[0]);
            this.getModule().getRecord().addLine(
                    SharedObjects.DATE_FORMAT.format(new Date()),
                    args[0],
                    sender.getName(),
                    rec[0],
                    rec[1],
                    rec[2]
            );
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("<uuid>");
            }
        }
    }
}

