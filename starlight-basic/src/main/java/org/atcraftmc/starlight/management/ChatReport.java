package org.atcraftmc.starlight.management;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.math.SHA;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.api.ChatReportedEvent;
import org.atcraftmc.starlight.api.CustomChatRenderer;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.starlight.core.ui.TextRenderer;
import org.atcraftmc.starlight.data.record.RecordService;
import org.atcraftmc.starlight.data.record.registry.DataRenderer;
import org.atcraftmc.starlight.data.record.registry.RecordField;
import org.atcraftmc.starlight.data.record.registry.RecordRegistry;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.module.BlacklistPlatform;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.*;

@ApplicationModule(id = "chat-report", version = "1.0.0")
@CommandProvider(ChatReport.ChatReportCommand.class)
@AutoRegister(Registrations.SERVER_EVENT)
@BlacklistPlatform({APIProfile.SPIGOT, APIProfile.BUKKIT, APIProfile.ARCLIGHT})
public final class ChatReport extends SLPackageModule {
    private static final RecordRegistry.A5<String, String, String, String, String> RECORD = new RecordRegistry.A5<>(
            "chat-report",
            new RecordField<>("op-id", TextRenderer.literal("Operation ID"), DataRenderer.STRING),
            new RecordField<>("reporter", TextRenderer.literal("Reporter"), DataRenderer.STRING),
            new RecordField<>("send-time", TextRenderer.literal("Send Time"), DataRenderer.STRING),
            new RecordField<>("sender", TextRenderer.literal("Sender"), DataRenderer.STRING),
            new RecordField<>("text", TextRenderer.literal("Chat Content"), DataRenderer.STRING)
    );

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        var sender = event.getPlayer().getName();
        var time = SharedObjects.DATE_FORMAT.format(new Date());
        var content = ComponentSerializer.plain(event.message());

        var shorted = (content.substring(0, Math.min(content.length(), 4)) + "...").replace(" ", "");
        var uuid = hash(content);
        this.records.put(uuid, "%s;%s;%s;%s".formatted(time, sender, content, shorted));

        var template = Objects.requireNonNull(config().value("append").string()).formatted(uuid);
        CustomChatRenderer.renderer(event).postfixNearest(TextBuilder.buildComponent(template));
    }

    @QuarkCommand(name = "chat-report")
    public static final class ChatReportCommand extends ModuleCommand<ChatReport> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            var handle = this.getModule();

            if (!handle.records.containsKey(args[0])) {
                MessageAccessor.send(this.getLanguage(), sender, "not-exist");
                return;
            }

            String[] rec = handle.records.get(args[0]).split(";");

            if (rec[1].equalsIgnoreCase(sender.getName())) {
                MessageAccessor.send(this.getLanguage(), sender, "not-self");
                return;
            }

            handle.records.remove(args[0]);
            handle.language().item("success").send(sender, rec[1], rec[3]);
            RecordService.record(RECORD.render(args[0], sender.getName(), rec[1], rec[0], rec[2]));

            BukkitUtil.callEvent(new ChatReportedEvent(rec[1], rec[2], rec[3], args[0]), (e) -> {
                if (e.getOutcome() == null) {
                    return;
                }
                e.getOutcome().send(sender, rec[1], rec[3]);
            });
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("<uuid>");
            }
        }
    }
}

