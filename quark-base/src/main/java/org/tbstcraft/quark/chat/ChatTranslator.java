package org.tbstcraft.quark.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.CustomChatRenderer;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.data.PlaceHolderStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.HashSet;
import java.util.Objects;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "chat-translator", version = "_dev", beta = true)
public final class ChatTranslator extends PackageModule {
    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.add(this.tip));
    }

    @Override
    public void disable() {
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.remove(this.tip));
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String msg = PlainTextComponentSerializer.plainText().serialize(event.message());
        String template = Objects.requireNonNull(getConfig().getString("append")).formatted(msg);
        CustomChatRenderer.renderer(event).postfix(TextBuilder.buildComponent(template));
    }
}
