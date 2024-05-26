package org.tbstcraft.quark.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.CustomChatRenderer;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.text.TextBuilder;
import org.tbstcraft.quark.util.platform.APIProfile;

import java.util.*;

@QuarkModule(
        version = "1.0.0",
        compatBlackList = {APIProfile.SPIGOT, APIProfile.BUKKIT, APIProfile.ARCLIGHT},
        recordFormat = {"Time", "OperationID", "Reporter", "SendTime", "Sender", "Content"}
)
@CommandProvider(ChatReport.ChatReportCommand.class)
@ModuleService(ServiceType.EVENT_LISTEN)
public class ChatReport extends PackageModule {
    private final Map<String, String> records = new HashMap<>();

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String uuid = UUID.randomUUID().toString().split("-")[4];
        String sender = event.getPlayer().getName();
        String time = SharedObjects.DATE_FORMAT.format(new Date());
        String content = LegacyComponentSerializer.legacySection().serialize(event.message());

        String template = Objects.requireNonNull(getConfig().getString("append")).formatted(uuid);

        CustomChatRenderer.renderer(event).postfix(TextBuilder.buildComponent(template));

        this.records.put(uuid, "%s;%s;%s".formatted(time, sender, content));
    }

    @QuarkCommand(name = "chat-report")
    public static final class ChatReportCommand extends ModuleCommand<ChatReport> {
        //todo:用chat-renderer

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (!this.getModule().records.containsKey(args[0])) {
                this.getLanguage().sendMessageTo(sender, "not-exist");
                return;
            }

            String[] rec = this.getModule().records.get(args[0]).split(";");
            this.getModule().records.remove(args[0]);
            this.getLanguage().sendMessageTo(sender, "success", rec[1], args[0]);
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

