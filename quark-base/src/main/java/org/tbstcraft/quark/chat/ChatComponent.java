package org.tbstcraft.quark.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.compat.Compat;
import org.tbstcraft.quark.framework.module.compat.CompatContainer;
import org.tbstcraft.quark.framework.module.compat.CompatDelegate;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.placeholder.PlaceHolders;
import org.tbstcraft.quark.util.placeholder.GloballyPlaceHolder;


@AutoRegister(ServiceType.EVENT_LISTEN)
@Compat(ChatComponent.PaperCompat.class)
@QuarkModule(id = "chat-component", version = "1.3.0")
public final class ChatComponent extends PackageModule {
    GloballyPlaceHolder chat = PlaceHolders.chat();

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
    public void onChatting(AsyncPlayerChatEvent event) {
        if (APIProfileTest.isPaperCompat()) {
            return;
        }
        String msg = event.getMessage();
        msg = PlaceHolderService.format(msg);
        msg = processChar(msg);
        msg = processColorChars(msg);
        event.setMessage(TextBuilder.build(msg).toString());
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (CommandManager.isQuarkCommand(event.getMessage().split(" ")[0].replace("/", ""))) {
            return;
        }
        if (event.getMessage().contains("self-msg")) {
            return;
        }
        String msg = event.getMessage();
        msg = PlaceHolderService.format(msg);
        msg = processChar(msg);
        msg = processColorChars(msg);
        event.setMessage(TextBuilder.build(msg).toString());
    }

    public String processChar(String input) {
        StringBuilder output = new StringBuilder();
        int processedIndex = 0;

        int cap = input.length();
        for (int i = 0; i < cap; i++) {
            char current = input.charAt(i);

            if (i < processedIndex) {
                continue;
            }
            if (current != '\\') {
                processedIndex += 1;
                output.append(current);
                continue;
            }
            if (cap - i < 2) {
                processedIndex += 1;
                output.append(current);
                continue;
            }
            char next = input.charAt(i + 1);
            if (next != 'u') {
                processedIndex += 2;
                switch (next) {
                    case 'n' -> output.append('\n');
                    case 't' -> output.append('\t');
                    case 'r' -> output.append('\r');
                    case 'b' -> output.append('\b');
                    case 'f' -> output.append('\f');
                    case '\'' -> output.append('\'');
                    case '\"' -> output.append('\"');
                    default -> output.append('\\');
                }
                continue;
            }
            processedIndex += 6;
            String sequence = input.substring(i + 2, i + 6);
            output.append(((char) Integer.parseInt(sequence, 16)));
        }

        return output.toString();
    }

    public String processColorChars(String input) {
        for (String s : chat.getRegisterKeys()) {
            input = input.replace(s, chat.get(s));
        }
        return input;
    }

    @CompatDelegate(APIProfile.PAPER)
    @AutoRegister(ServiceType.EVENT_LISTEN)
    public static final class PaperCompat extends CompatContainer<ChatComponent> {
        public PaperCompat(ChatComponent parent) {
            super(parent);
        }

        @EventHandler
        public void onChatting(AsyncChatEvent event) {
            String msg = LegacyComponentSerializer.legacySection().serialize(event.message());
            msg = getParent().processColorChars(msg);
            msg = PlaceHolderService.format(msg);
            msg = this.getParent().processChar(msg);
            event.message(TextBuilder.buildComponent(msg));
        }

        @EventHandler
        public void onSignEdit(SignChangeEvent event) {
            for (int i = 0; i < event.lines().size(); i++) {
                Component origin = event.line(i);
                if (origin == null) {
                    continue;
                }

                String text = LegacyComponentSerializer.legacySection().serialize(origin);
                text = this.getParent().processColorChars(text);
                text = this.getParent().processChar(text);

                event.line(i, TextBuilder.buildComponent(text));
            }
        }
    }
}
