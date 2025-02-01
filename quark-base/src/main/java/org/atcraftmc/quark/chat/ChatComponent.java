package org.atcraftmc.quark.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO2;
import me.gb2022.commons.reflect.method.MethodHandleRO1;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.qlib.texts.placeholder.GloballyPlaceHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.foundation.ComponentSerializer;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.BukkitDataAccess;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.component.Components;
import org.tbstcraft.quark.framework.module.component.ModuleComponent;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.placeholder.PlaceHolders;

import java.util.Objects;
import java.util.regex.Pattern;


@AutoRegister(ServiceType.EVENT_LISTEN)
@Components({ChatComponent.PaperChatListener.class, ChatComponent.PaperSignChangeListener.class})
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
        msg = PlaceHolderService.formatPlayer(event.getPlayer(), msg);
        msg = processChar(msg);
        msg = processColorChars(msg);
        event.setMessage(TextBuilder.build(msg, true).toString());
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (LegacyCommandManager.isQLibCommand(event.getMessage().split(" ")[0].replace("/", ""))) {
            return;
        }
        if (event.getMessage().contains("self-msg")) {
            return;
        }
        String msg = event.getMessage();
        msg = PlaceHolderService.format(msg);
        msg = processChar(msg);
        msg = processColorChars(msg);
        event.setMessage(TextBuilder.build(msg, false).toString());
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

    @AutoRegister(ServiceType.EVENT_LISTEN)
    public static final class PaperSignChangeListener extends ModuleComponent<ChatComponent> {
        private MethodHandleO2<SignChangeEvent, Integer, Component> setLine;
        private MethodHandleRO1<SignChangeEvent, String, Integer> getLine;

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("org.bukkit.event.block.SignChangeEvent"));
        }

        @Override
        public void enable() {
            var t = SignChangeEvent.class;

            this.setLine = MethodHandle.select((ctx) -> {
                //noinspection Convert2MethodRef
                ctx.attempt(() -> t.getMethod("line", int.class, Component.class), (o, i, c) -> o.line(i, c));
                ctx.dummy((o, i, c) -> o.setLine(i, ComponentSerializer.legacy(c)));
            });
            this.getLine = MethodHandle.select((ctx) -> {
                ctx.attempt(() -> t.getMethod("line", int.class), (o, i) -> ComponentSerializer.legacy(Objects.requireNonNullElse(o.line(i), Component.text(""))));
                //noinspection Convert2MethodRef
                ctx.dummy((o, i) -> o.getLine(i));
            });
        }

        @EventHandler
        public void onSignEdit(SignChangeEvent event) {
            for (int i = 0; i < event.lines().size(); i++) {
                var text = this.getLine.invoke(event, i);
                text = this.parent.processColorChars(text);
                text = this.parent.processChar(text);

                var matcher = Pattern.compile("§.").matcher(text);

                while (matcher.find()) {
                    var res = matcher.group();

                    text = text.replace(res, "{#&" + res.substring(1) + "}");
                }

                this.setLine.invoke(event, i, TextBuilder.buildComponent(text, false));
            }
        }
    }

    @AutoRegister(ServiceType.EVENT_LISTEN)
    public static final class PaperChatListener extends ModuleComponent<ChatComponent> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("io.papermc.paper.event.player.AsyncChatEvent"));
        }

        @EventHandler
        public void onChatting(AsyncChatEvent event) {
            String msg = LegacyComponentSerializer.legacySection().serialize(event.message());
            msg = this.parent.processColorChars(msg);
            msg = PlaceHolderService.format(msg);
            msg = PlaceHolderService.formatPlayer(event.getPlayer(), msg);
            msg = this.parent.processChar(msg);
            event.message(TextBuilder.buildComponent(msg, true));
        }

        @EventHandler
        public void onAnvilRename(PrepareAnvilEvent event) {
            AnvilInventory anvilInventory = event.getInventory();
            ItemStack resultItem = event.getResult();
            if (resultItem == null) {
                return;
            }

            var renameText = anvilInventory.getRenameText();

            if (renameText == null || renameText.isEmpty()) {
                return;
            }

            BukkitDataAccess.itemMeta(resultItem, (meta) -> {
                var msg = renameText;
                msg = this.parent.processColorChars(msg);
                msg = PlaceHolderService.format(msg);
                msg = PlaceHolderService.formatPlayer(((Player) event.getViewers().get(0)), msg);
                msg = this.parent.processChar(msg);

                meta.displayName(TextBuilder.buildComponent(msg, false));
            });
        }
    }
}
