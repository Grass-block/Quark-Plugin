package org.atcraftmc.starlight.music;

import me.gb2022.apm.remote.RemoteMessenger;
import me.gb2022.apm.remote.event.APMRemoteEvent;
import me.gb2022.apm.remote.event.message.RemoteMessageEvent;
import me.gb2022.apm.remote.event.message.RemoteMessageSurpassEvent;
import me.gb2022.apm.remote.event.message.RemoteQueryEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.subcomponent.ComponentProvider;
import me.gb2022.simpnet.util.BufferUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.RemoteMessageService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.ui.InventoryUI;
import org.atcraftmc.starlight.core.ui.TextRenderer;
import org.atcraftmc.starlight.core.ui.UI;
import org.atcraftmc.starlight.core.ui.providing.GUIProvider;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.module.SLModuleComponent;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.music.resolve.MusicData;
import org.atcraftmc.starlight.music.resolve.MusicResolveRequest;
import org.atcraftmc.starlight.music.session.LegacyMusicSession;
import org.atcraftmc.starlight.music.session.MusicSession;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@CommandProvider(MusicPlayer.MusicCommand.class)
@AutoRegister({Registrations.SERVER_EVENT, Registrations.PLUGIN_VPN_EVENT})
@ComponentProvider({MusicPlayer.APMEventHandler.class, MusicPlayer.PlayerEventHandler.class})
@ApplicationModule(id = "music-player", version = "1.0.3")
public final class MusicPlayer extends SLPackageModule implements PlayerUIRenderer {
    private final GUIProvider<InventoryUI> musicUI = new MusicUI(this);
    private MusicSession globalSession;

    @Inject
    private LanguageEntry language;

    @Override
    public void renderUI(Player player, MusicData currentMusic, int currentTick, boolean pause) {
        String template = Language.generateTemplate(this.config(), "ui", (s) -> {
            if (pause) {
                s = s.replace("{msg#playing}", "{msg#paused}");
            }
            return s;
        });
        template = template.replace("{name}", currentMusic.getName().replace("_", " ")).replace("{time}",
                                                                                                PlayerUIRenderer.formatTime(currentMusic.getMillsLength() * currentTick / currentMusic.getTickLength())
        ).replace(
                "{total}",
                PlayerUIRenderer.formatTime(currentMusic.getMillsLength())
        );

        String ui = this.language().inline(template, LocaleService.locale(player));
        TextSender.sendActionbarTitle(player, TextBuilder.build(ui));
    }

    @Override
    public void enable() {
        this.globalSession = new LegacyMusicSession(this);
        this.globalSession.startSession();

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.globalSession.addPlayer(player);
        }
    }

    @Override
    public void disable() {
        for (String s : new HashSet<>(TaskService.async().tasks())) {
            if (s.startsWith("quark_midi")) {
                TaskService.async().cancel(s);
            }
        }

        this.globalSession.destroySession();
    }


    public void pauseMusic(String player) {
        MessageAccessor.broadcast(this.language, false, false, "pause", player);
        this.globalSession.pause();
    }

    public void resumeMusic(String player) {
        MessageAccessor.broadcast(this.language, false, false, "resume", player);
        this.globalSession.resume();
    }

    public void cancelMusic(String player) {
        MessageAccessor.broadcast(this.language, false, false, "cancel", player);
        this.globalSession.cancel();
    }

    public void playMusic(MusicResolveRequest request) {
        this.globalSession.play(MusicService.instance().dispatch(request));
        MessageAccessor.broadcast(this.language, false, false, "play", request.actor(), request.music(), request.pitch());
    }

    //todo: 音乐获取指令拆分 管理部分移动到MusicFileService
    @QuarkCommand(name = "music", permission = "+quark.music.play")
    public static final class MusicCommand extends ModuleCommand<MusicPlayer> {
        @Override
        public void suggest(CommandSuggestion suggestion) {
            MusicCommandDispatcher.suggest(suggestion, 0);
        }

        @Override
        public void execute(CommandExecution context) {
            var service = RemoteMessageService.instance();
            var operator = context.getSender().getName();

            switch (context.requireEnum(0, "play", "pause", "resume", "cancel", "save-defaults", "gui", "trim")) {
                case "gui" -> {
                    var page = context.hasArgumentAt(1) ? context.requireArgumentInteger(1) : 0;
                    getModule().musicUI.open(context.requireSenderAsPlayer(), page);
                }
                case "trim" -> getLanguage().item("trim").send(context.getSender(), MusicService.instance().trim());
                case "save-defaults" -> {
                    MusicService.instance().saveDefaults();
                    MessageAccessor.send(this.getLanguage(), context.getSender(), "restore-defaults");
                }
                case "cancel" -> {
                    this.getModule().cancelMusic(operator);
                    service.broadcast("music:control", "cancel;" + operator);
                }
                case "pause" -> {
                    this.getModule().pauseMusic(operator);
                    service.broadcast("music:control", "pause;" + operator);
                }
                case "resume" -> {
                    this.getModule().resumeMusic(operator);
                    service.broadcast("music:control", "resume;" + operator);
                }
                case "play" -> {
                    var request = MusicCommandDispatcher.selectMusic(context, 0);
                    this.getModule().playMusic(request);

                    service.broadcast("music:control", msg -> {
                        String data = "play;%s;%s;%d;%s;%f".formatted(request.actor(),
                                                                      request.music(),
                                                                      request.pitch(),
                                                                      request.dispatchInstrument(),
                                                                      request.speedMod()
                        );
                        BufferUtil.writeString(msg, data);
                    });
                }
            }
        }
    }

    public static final class APMEventHandler extends SLModuleComponent<MusicPlayer> {
        @Override
        public void enable() {
            RemoteMessageService.instance().registerEventHandler(this);
        }

        @Override
        public void disable() {
            RemoteMessageService.instance().removeMessageHandler(this);
        }

        @APMRemoteEvent("music:get")
        public void onMusicFetch(RemoteMessenger ctx, RemoteQueryEvent event) {
            var file = MusicService.instance().loadFile(BufferUtil.readString(event.message()));

            try (var i = new FileInputStream(file)) {
                event.write((b) -> {
                    try {
                        BufferUtil.writeArray(b, i.readAllBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @APMRemoteEvent("music:list")
        public void onMusicList(RemoteMessenger ctx, RemoteQueryEvent event) {
            event.write(String.join(";", MusicService.instance().list().toArray(new String[0])));
        }

        @APMRemoteEvent("music:control")
        public void onMusicEvent(RemoteMessenger ctx, RemoteMessageEvent event) {
            var commands = event.decode(String.class).split(";");

            switch (commands[0]) {
                case "cancel" -> this.parent.cancelMusic(commands[1]);
                case "pause" -> this.parent.pauseMusic(commands[1]);
                case "resume" -> this.parent.resumeMusic(commands[1]);
                case "play" -> {
                    var player = commands[1];
                    var music = commands[2];
                    var pitch = Integer.parseInt(commands[3]);
                    var speed = Float.parseFloat(commands[5]);
                    var legacy = Boolean.parseBoolean(commands[4]);

                    var request = new MusicResolveRequest(player, music, pitch, legacy, speed, 0);
                    TaskService.async().run(() -> this.parent.playMusic(request));
                }
            }
        }

        @APMRemoteEvent("music:control")
        public void onMusicEvent(RemoteMessenger ctx, RemoteMessageSurpassEvent event) {
            this.onMusicEvent(ctx, ((RemoteMessageEvent) event));
        }
    }

    public static final class PlayerEventHandler extends SLModuleComponent<MusicPlayer> {
        @Override
        public void enable() {
            BukkitUtil.registerEventListener(this);
        }

        @Override
        public void disable() {
            BukkitUtil.unregisterEventListener(this);
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            this.parent.globalSession.addPlayer(event.getPlayer());
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            this.parent.globalSession.removePlayer(event.getPlayer());
        }
    }

    public final class MusicUI implements GUIProvider<InventoryUI> {
        private final MusicPlayer reference;

        public MusicUI(MusicPlayer reference) {
            this.reference = reference;
        }

        @Override
        public InventoryUI create() {
            return new InventoryUI(54, TextRenderer.literal(Component.text("__title")));
        }

        @Override
        public InventoryUIView initializeView(InventoryUI builder, Player viewer, Object... args) {
            var list = new ArrayList<>(MusicService.instance().list());
            list.sort(Comparator.naturalOrder());
            builder.title(TextRenderer.data(language().item("ui-title"), list.size() / 45));
            var view = builder.createInventoryUI(viewer);
            view.setCustomData("music-list", list);
            return view;
        }

        @Override
        public void render(InventoryUI builder, InventoryUIView view, Object... args) {
            var page = Integer.parseInt(args[0].toString());
            var legacy = view.getCustomData("legacy", Boolean.class, false);
            var list = ((List<String>) view.getCustomData("music-list", List.class));
            var pages = list.size() / 45;
            var base = page * 45;

            if (page > pages) {
                throw new RuntimeException("Page code out of range!");
            }

            for (var i = base; i < base + 45; i++) {
                if (i >= list.size()) {
                    break;
                }
                var id = list.get(i);
                var name = id.replace(".mid", "").replace(".midi", "").replace("_", " ");

                UI.buildComponent(builder, i - base, (b) -> {
                    var icon = UI.icon(Material.NOTE_BLOCK);
                    b.icon(icon);
                    b.name(TextRenderer.literal(Component.text(name).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));
                    b.lore(TextRenderer.literal(Component.text(id).decoration(TextDecoration.ITALIC, false)));
                    b.lore(TextRenderer.data(language().item("ui-click")));
                    b.operation(UI.SOUND_CLICK);
                    b.operation(UI.command(UI.value("music play " + id + (legacy ? " -legacy" : ""))));
                });
            }

            //toggle legacy control
            UI.buildComponent(builder, 45, (b) -> {
                b.icon(!legacy ? UI.icon(Material.REDSTONE_TORCH) : UI.enchanted(Material.REDSTONE_TORCH));
                b.name(TextRenderer.data(legacy ? this.reference.language.item("ui-legacy-control-open") : this.reference.language.item(
                        "ui-legacy-control-close")));
                b.operation((v, player, action) -> v.setCustomData("legacy", !legacy));
                b.operation((v, player, action) -> rebuildView(v, args));
                b.operation(UI.SOUND_CLICK);
            });

            //close button
            UI.buildComponent(builder, 53, (b) -> {
                b.icon(UI.icon(Material.REDSTONE));
                b.name(TextRenderer.data(Starlight.lang().item("common", "ui", "close")));
                b.operation(UI.SOUND_CLICK);
                b.operation(UI.close());
            });

            //cancel button
            UI.buildComponent(builder, 52, (b) -> {
                b.icon(UI.icon(Material.MUSIC_DISC_11));
                b.name(TextRenderer.data(language().item("ui-cancel")));
                b.operation(UI.SOUND_CLICK);
                b.operation(UI.command(UI.value("music cancel")));
            });


            //page indicator
            UI.buildComponent(builder, 49, (b) -> {
                b.icon(UI.icon(Material.CLOCK, page + 1));
                b.name(TextRenderer.data(Starlight.lang().item("common", "ui", "page"), page + 1, pages + 1));
            });

            if (page != 0) {
                UI.builder()
                        .icon(UI.icon(Material.YELLOW_STAINED_GLASS_PANE))
                        .name(TextRenderer.data(Starlight.lang().item("common", "ui", "prev")))
                        .operation((v, player, action) -> v.setData(renderData(v, page - 1)))
                        .operation(UI.SOUND_CLICK)
                        .build(builder, 48);
            } else {
                UI.builder()
                        .icon(UI.icon(Material.GRAY_STAINED_GLASS_PANE))
                        .name(TextRenderer.data(Starlight.lang()
                                                        .item("common", "ui", "prev")))
                        .operation(UI.SOUND_DISABLE)
                        .build(builder, 48);
            }

            if (page != pages) {
                UI.builder()
                        .icon(UI.icon(Material.BLUE_STAINED_GLASS_PANE))
                        .name(TextRenderer.data(Starlight.lang()
                                                        .item("common", "ui", "next")))
                        .operation((v, player, action) -> v.setData(renderData(v, page + 1)))
                        .operation(UI.SOUND_CLICK)
                        .build(builder, 50);
            } else {
                UI.builder()
                        .icon(UI.icon(Material.GRAY_STAINED_GLASS_PANE))
                        .name(TextRenderer.data(Starlight.lang()
                                                        .item("common", "ui", "next")))
                        .operation(UI.SOUND_DISABLE)
                        .build(builder, 50);
            }
        }
    }
}