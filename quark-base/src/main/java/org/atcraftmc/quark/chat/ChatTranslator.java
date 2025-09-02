package org.atcraftmc.quark.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.atcraftmc.starlight.api.CustomChatRenderer;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;

import java.util.Objects;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(id = "chat-translator", version = "_dev", beta = true)
public class ChatTranslator extends PackageModule {
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

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        var msg = PlainTextComponentSerializer.plainText().serialize(event.message());
        var template = Objects.requireNonNull(getConfig().value("append").string()).formatted(msg);
        CustomChatRenderer.renderer(event).postfix(TextBuilder.buildComponent(template));
    }
}
