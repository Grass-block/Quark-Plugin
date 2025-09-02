package org.atcraftmc.quark.contents.music;

import me.gb2022.apm.remote.RemoteMessenger;
import me.gb2022.apm.remote.event.APMRemoteEvent;
import me.gb2022.apm.remote.event.message.RemoteMessageEvent;
import me.gb2022.apm.remote.event.message.RemoteMessageSurpassEvent;
import me.gb2022.apm.remote.event.message.RemoteQueryEvent;
import me.gb2022.simpnet.util.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.Starlight;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.atcraftmc.starlight.data.assets.AssetGroup;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.RemoteMessageService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.ui.InventoryUI;
import org.atcraftmc.starlight.core.ui.TextRenderer;
import org.atcraftmc.starlight.core.ui.UI;
import org.atcraftmc.starlight.core.ui.providing.GUIProvider;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;

import java.io.IOException;
import java.util.*;

@CommandProvider(MusicPlayer.MusicCommand.class)
@SLModule(version = "1.0.3")
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.REMOTE_MESSAGE})
@Components({MusicPlayer.APMEventHandler.class, MusicPlayer.PlayerEventHandler.class})
public final class MusicPlayer extends PackageModule {
    public static final String UNSUPPORTED_FORMAT = "unsupported-format";
    public static final String RESOLVE_ERROR = "error-resolving";
    public static final String NOT_FOUND = "not-found";
    public static final String TIMEOUT = "timeout";
    private static final MethodHandleO3<Player, Sound, Float, Float> PLAY_NOTE = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> {
            Class.forName("org.bukkit.SoundCategory");
            return null;
        }, (p, s, power, pitch) -> p.playSound(p.getLocation(), s, SoundCategory.RECORDS, power, pitch));
        ctx.dummy((p, s, power, pitch) -> p.playSound(p.getLocation(), s, power, pitch));
    });

    private final GUIProvider<InventoryUI> musicUI = new MusicUI(this);
    private final MusicSession globalSession = new MusicSession(this);
    private MusicFileLoader loader;

    @Inject("music")
    private AssetGroup musicGroup;

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() {
        if (!this.musicGroup.existFolder()) {
            this.saveDefaults();
        }

        this.globalSession.startSession();

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.globalSession.addPlayer(player);
        }

        if (ConfigAccessor.getBool(this.getConfig(), "remote")) {
            this.loader = new MusicFileLoader.RemoteLoader(this.musicGroup, this.getConfig().value("cdn-server").string());
        } else {
            this.loader = new MusicFileLoader.LocalLoader(this.musicGroup);
        }
    }

    @Override
    public void disable() {
        for (String s : new HashSet<>(TaskService.async().tasks())) {
            if (s.startsWith("quark_midi")) {
                TaskService.async().cancel(s);
            }
        }
        this.globalSession.stopSession();
    }

    public void saveDefaults() {
        this.musicGroup.save("Avicii-The_Days.mid");
        this.musicGroup.save("Avicii-The_Nights.mid");
        this.musicGroup.save("Avicii-Waiting_For_Love.mid");
        this.musicGroup.save("Kiss_The_Rain.mid");
        this.musicGroup.save("Beyond-海阔天空.mid");
    }

    public void pauseMusic(String player) {
        MessageAccessor.broadcast(this.language, false, false, "cancel", player);
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

    public void playMusic(String player, String music, int pitch, boolean dispatchInstrument, float speedMod, int interpolation) {
        this.globalSession.play(select(music, pitch, dispatchInstrument, speedMod, interpolation));
        MessageAccessor.broadcast(this.language, false, false, "play", player, music, pitch);
    }

    public void playNode(Set<Player> audience, int node, int off, EnumInstrument targetInstrument, float power) {
        int base = node - 23 + off - 6;//wtf
        if (base < 0 || base >= 72) {
            return;
        }

        var remapped = switch (targetInstrument) {
            case GUITAR ->
                    base > 0 ? base < 24 ? EnumInstrument.BASS_GUITAR : base >= 48 ? EnumInstrument.XYLOPHONE : EnumInstrument.GUITAR : EnumInstrument.BASS_DRUM;
            case PIANO ->
                    base > 0 ? base < 24 ? EnumInstrument.BASS_GUITAR : base >= 48 ? EnumInstrument.BELL : EnumInstrument.PIANO : EnumInstrument.BASS_DRUM;
            case STD_DRUM -> switch (node) {
                case 38, 40 -> EnumInstrument.SNARE_DRUM;
                case 42, 44, 46, 49, 51 -> EnumInstrument.HAT;
                default -> EnumInstrument.BASS_DRUM;//35,36
            };


            default -> targetInstrument;
        };


        var off1 = base % 12;
        var octave = base % 24 > 11 ? 1 : 0;

        var n = switch (off1) {
            case 6 -> Note.natural(octave, Note.Tone.C);
            case 7 -> Note.sharp(octave, Note.Tone.C);
            case 8 -> Note.natural(octave, Note.Tone.D);
            case 9 -> Note.sharp(octave, Note.Tone.D);
            case 10 -> Note.natural(octave, Note.Tone.E);
            case 11 -> Note.natural(octave, Note.Tone.F);
            case 0 -> Note.sharp(octave, Note.Tone.F);
            case 1 -> Note.natural(octave, Note.Tone.G);
            case 2 -> Note.sharp(octave, Note.Tone.G);
            case 3 -> Note.natural(octave, Note.Tone.A);
            case 4 -> Note.sharp(octave, Note.Tone.A);
            case 5 -> Note.natural(octave, Note.Tone.B);
            default -> throw new IllegalStateException("Unexpected value: " + off1);
        };

        float pitch = (float) Math.pow(2.0, (n.getId() - 12) / 12.0);

        for (Player p : audience) {
            PLAY_NOTE.invoke(p, remapped.bukkit(), power, pitch);
        }
    }

    private MusicData select(String name, int pitch, boolean dispatchInstrument, float speedMod, int interpolation) {
        var f = this.loader.load(name);

        if (!f.exists()) {
            throw new IllegalArgumentException(NOT_FOUND);
        }

        return MusicResolver.resolve(f, pitch, dispatchInstrument, speedMod, interpolation);
    }

    @QuarkCommand(name = "music", permission = "+quark.music.play")
    public static final class MusicCommand extends ModuleCommand<MusicPlayer> {
        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "play", "pause", "resume", "cancel", "gui", "save-defaults", "trim");
            suggestion.matchArgument(0, "play", (ctx) -> {
                ctx.suggest(1, this.getModule().loader.list());
                ctx.suggest(1, "random");

                for (var i = 2; i < 8; i++) {
                    ctx.suggest(i, "-p:0");
                    ctx.suggest(i, "-p:12");
                    ctx.suggest(i, "-p:-12");
                    ctx.suggest(i, "-s:1");
                    ctx.suggest(i, "-s:1.5");
                    ctx.suggest(i, "-s:0.5");
                    ctx.suggest(i, "-legacy");
                    ctx.suggest(i, "-i:on");
                    ctx.suggest(i, "-i:smart");
                }
            });
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
                case "trim" -> getLanguage().item("trim").send(context.getSender(), getModule().loader.trim());
                case "save-defaults" -> {
                    this.getModule().saveDefaults();
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
                    var args = context.getArgs();

                    int pitch = 0;
                    float speedMod = 1.0f;

                    ConfigurationSection pitchOffsets = this.getConfig().value("pitch-offsets").section();
                    if (pitchOffsets != null && pitchOffsets.contains(args[1])) {
                        pitch = pitchOffsets.getInt(args[1]);
                    }

                    boolean legacy = !List.of(args).contains("-legacy");
                    int interpolation = 0;

                    for (String s : args) {
                        if (s.startsWith("-p:")) {
                            pitch = Integer.parseInt(s.replace("-p:", ""));
                        }
                        if (s.startsWith("-s:")) {
                            speedMod = Float.parseFloat(s.replace("-s:", ""));
                        }
                        if (s.startsWith("-i:")) {
                            interpolation = s.replace("-i:", "").equals("smart") ? 1 : 2;
                        }
                    }

                    String music;

                    if (Objects.equals(args[1], "random")) {
                        music = getModule().loader.random();
                    } else {
                        music = args[1];
                    }

                    if (!this.getModule().loader.list().contains(music)) {
                        MessageAccessor.send(this.getLanguage(), context.getSender(), "not-found", music);
                        return;
                    }

                    this.getModule().playMusic(operator, music, pitch, legacy, speedMod, interpolation);

                    int finalPitch = pitch;
                    float finalSpeedMod = speedMod;

                    service.broadcast("music:control", msg -> {
                        String data = "play;%s;%s;%d;%s;%f".formatted(operator, music, finalPitch, legacy, finalSpeedMod);
                        BufferUtil.writeString(msg, data);
                    });
                }
            }
        }
    }

    public static final class APMEventHandler extends ModuleComponent<MusicPlayer> {
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
            this.parent.musicGroup.asInputStream(BufferUtil.readString(event.message()), (stream -> event.write((b) -> {
                try {
                    BufferUtil.writeArray(b, stream.readAllBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })));
        }

        @APMRemoteEvent("music:list")
        public void onMusicList(RemoteMessenger ctx, RemoteQueryEvent event) {
            event.write(String.join(";", this.parent.musicGroup.list().toArray(new String[0])));
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
                    TaskService.async().run(() -> this.parent.playMusic(player, music, pitch, legacy, speed, 0));
                }
            }
        }

        @APMRemoteEvent("music:control")
        public void onMusicEvent(RemoteMessenger ctx, RemoteMessageSurpassEvent event) {
            this.onMusicEvent(ctx, ((RemoteMessageEvent) event));
        }
    }

    public static final class PlayerEventHandler extends ModuleComponent<MusicPlayer> {
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
            var list = new ArrayList<>(this.reference.loader.list());
            list.sort(Comparator.naturalOrder());
            builder.title(TextRenderer.data(getLanguage().item("ui-title"), list.size() / 45));
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
                    b.lore(TextRenderer.data(getLanguage().item("ui-click")));
                    b.operation(UI.SOUND_CLICK);
                    b.operation(UI.command(UI.value("music play " + id + (legacy ? " -legacy" : ""))));
                });
            }

            //toggle legacy control
            UI.buildComponent(builder, 45, (b) -> {
                b.icon(!legacy ? UI.icon(Material.REDSTONE_TORCH) : UI.enchanted(Material.REDSTONE_TORCH));
                b.name(TextRenderer.data(legacy ? getLanguage().item("ui-legacy-control-open") : getLanguage().item(
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
                b.name(TextRenderer.data(getLanguage().item("ui-cancel")));
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
                        .name(TextRenderer.data(Starlight.lang().item("common", "ui", "prev")))
                        .operation(UI.SOUND_DISABLE)
                        .build(builder, 48);
            }

            if (page != pages) {
                UI.builder()
                        .icon(UI.icon(Material.BLUE_STAINED_GLASS_PANE))
                        .name(TextRenderer.data(Starlight.lang().item("common", "ui", "next")))
                        .operation((v, player, action) -> v.setData(renderData(v, page + 1)))
                        .operation(UI.SOUND_CLICK)
                        .build(builder, 50);
            } else {
                UI.builder()
                        .icon(UI.icon(Material.GRAY_STAINED_GLASS_PANE))
                        .name(TextRenderer.data(Starlight.lang().item("common", "ui", "next")))
                        .operation(UI.SOUND_DISABLE)
                        .build(builder, 50);
            }
        }
    }
}