package org.tbstcraft.quark.display;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.module.*;
import org.tbstcraft.quark.module.compat.Compat;
import org.tbstcraft.quark.module.compat.CompatContainer;
import org.tbstcraft.quark.module.compat.CompatDelegate;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.text.TextBuilder;
import org.tbstcraft.quark.util.api.APIProfile;

@EventListener
@QuarkModule(version = "1.2.0")
@Compat(ChatFormat.PaperCompat.class)
public final class ChatFormat extends PackageModule {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLegacyPlayerChat(AsyncPlayerChatEvent event) {
        String template = this.getConfig().getString("template");
        if (template == null) {
            return;
        }

        Component c = TextBuilder.buildComponent(template, Component.text("%1$s"), Component.text("%2$s"));
        event.setFormat(LegacyComponentSerializer.legacySection().serialize(c));
    }

    @CompatDelegate(APIProfile.PAPER)
    public static final class PaperCompat extends CompatContainer<ChatFormat> {
        private final Renderer renderer;

        public PaperCompat(ChatFormat parent) {
            super(parent);
            this.renderer = new Renderer(this.getParent().getConfig());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChat(AsyncChatEvent event) {
            if (this.getConfig().getString("template") == null) {
                return;
            }
            event.renderer(this.renderer);
        }

        @SuppressWarnings("ClassCanBeRecord")
        private static class Renderer implements io.papermc.paper.chat.ChatRenderer {
            private final ConfigurationSection config;

            private Renderer(ConfigurationSection config) {
                this.config = config;
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public Component render(Player player, Component displayName, Component content, Audience audience) {
                String template = this.config.getString("template");
                return TextBuilder.buildComponent(template, displayName, content);
            }
        }
    }
}

