package org.tbstcraft.quark.contents;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.client.event.ClientRequestEvent;
import me.gb2022.apm.client.event.driver.ClientEventHandler;
import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.event.remote.RemoteQueryEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.contents.musics.EnumInstrument;
import org.tbstcraft.quark.contents.musics.MusicData;
import org.tbstcraft.quark.contents.musics.MusicFileLoader;
import org.tbstcraft.quark.contents.musics.MusicSession;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ClientMessageListener;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.services.RemoteMessageListener;
import org.tbstcraft.quark.service.base.task.TaskService;
import org.tbstcraft.quark.service.network.RemoteMessageService;
import org.tbstcraft.quark.service.network.http.HttpHandlerContext;
import org.tbstcraft.quark.service.network.http.HttpRequest;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.api.PlayerUtil;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@EventListener
@RemoteMessageListener
@ClientMessageListener
@CommandRegistry(MusicPlayer.MusicCommand.class)
@QuarkModule(version = "1.0.3")
public class MusicPlayer extends PackageModule {
    public static final String UNSUPPORTED_FORMAT = "unsupported-format";
    public static final String RESOLVE_ERROR = "error-resolving";
    public static final String NOT_FOUND = "not-found";
    public static final String TIMEOUT = "timeout";

    private final MusicSession globalSession = new MusicSession(this);

    private MusicFileLoader loader;

    @Override
    public void enable() {
        getFolder();

        release("avicii-Waiting_For_Love.mid");
        release("Beyond-海阔天空.mid");

        this.globalSession.startSession();
        this.globalSession.getPlayers().addAll(Bukkit.getOnlinePlayers());

        String folder = this.getFolder().getAbsolutePath();
        if (this.getConfig().getBoolean("remote")) {
            this.loader = new MusicFileLoader.RemoteLoader(folder, this.getConfig().getString("cdn-server"));
        } else {
            this.loader = new MusicFileLoader.LocalLoader(folder);
        }
    }

    @Override
    public void disable() {
        for (String s : new HashSet<>(TaskService.getAllTasks().keySet())) {
            if (s.startsWith("quark_midi")) {
                TaskService.cancelTask(s);
            }
        }
        this.globalSession.stopSession();
    }


    private void release(String name) {
        FilePath.tryReleaseAndGetFile("/assets/%s".formatted(name), getFolder().getAbsolutePath() + "/%s".formatted(name));
    }

    private File getFolder() {
        File file = new File(FilePath.pluginFolder(Quark.PLUGIN_ID) + "/midi/");
        if (!file.exists()) {
            if (file.mkdirs()) {
                this.getLogger().info("created midi folder.");
            }
        }
        return file;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.globalSession.getPlayers().add(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        this.globalSession.getPlayers().remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        this.globalSession.getPlayers().add(event.getPlayer());
    }


    @ClientEventHandler("/quark/music/list")
    public void onMusicFetch(ClientRequestEvent event) {
        JsonArray array = new JsonArray();
        for (String s : this.loader.list()) {
            array.add(s);
        }
        ClientMessenger.sendResponse(event.getPlayer(), "/quark/music/list", array);
    }

    @RemoteEventHandler("/music/get")
    public void onMusicFetch(RemoteQueryEvent event) {
        String fileName = BufferUtil.readString(event.getData());

        File f = new File(this.getFolder().getAbsolutePath() + "/" + fileName);

        if (!f.exists()) {
            return;
        }

        try (InputStream is = new FileInputStream(f)) {
            BufferUtil.writeArray(event.getResult(), is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RemoteEventHandler("/music/list")
    public void onMusicList(RemoteQueryEvent event) {
        StringBuilder sb = new StringBuilder();

        File[] objects = Objects.requireNonNull(this.getFolder().listFiles());
        for (int i = 0; i < objects.length; ++i) {
            sb.append(objects[i].getName());
            if (i != objects.length - 1) {
                sb.append(';');
            }
        }

        BufferUtil.writeString(event.getResult(), sb.toString());
    }

    @RemoteEventHandler("/music/control")
    public void onMusicEvent(RemoteMessageEvent event) {
        String[] args = BufferUtil.readString(event.getData()).split(";");

        switch (args[0]) {
            case "cancel" -> cancelMusic(args[1]);
            case "pause" -> pauseMusic(args[1]);
            case "resume" -> resumeMusic(args[1]);
            case "play" -> playMusic(args[1], args[2], Integer.parseInt(args[3]));
        }
    }

    @HttpRequest("/music/list")
    public JsonElement queryMusics(HttpHandlerContext context) {
        JsonArray array = new JsonArray();
        for (String s : this.loader.list()) {
            array.add(s);
        }
        return array;
    }


    public void pauseMusic(String player) {
        this.getLanguage().broadcastMessage(false, "pause", player);
        this.globalSession.pause();
    }

    public void resumeMusic(String player) {
        this.getLanguage().broadcastMessage(false, "resume", player);
        this.globalSession.resume();
    }

    public void cancelMusic(String player) {
        this.getLanguage().broadcastMessage(false, "cancel", player);
        this.globalSession.cancel();
    }

    public void playMusic(String player, String music, int pitch) {
        this.globalSession.play(select(music, pitch));
        this.getLanguage().broadcastMessage(false, "play", player, music, pitch);
    }


    public String getRandomMusicName() {
        List<String> music = this.loader.list();
        int index = SharedObjects.RANDOM.nextInt(music.size());
        return music.get(index);
    }

    public void playNode(Player player, int node, int off, EnumInstrument targetInstrument) {
        int base = node - 23 + off - 6;
        if (base < 0 || base >= 72) {
            return;
        }
        Instrument instrument = switch (targetInstrument) {
            case BASS -> Instrument.BASS_DRUM;
            case SNARE -> Instrument.SNARE_DRUM;
            default ->
                    base > 0 ? base < 24 ? Instrument.BASS_GUITAR : base >= 48 ? Instrument.BELL : Instrument.PIANO : Instrument.BASS_DRUM;
        };


        int off1 = base % 12;
        int octave = base % 24 > 11 ? 1 : 0;

        Note n = switch (off1) {
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

        player.playNote(player.getLocation(), instrument, n);
    }

    private MusicData select(String name, int pitch) {
        File f = this.loader.load(name);

        if (!f.exists()) {
            throw new IllegalArgumentException(NOT_FOUND);
        }

        if (name.endsWith(".mid") || name.endsWith(".midi")) {
            try {
                return MusicData.fromMidi(f.getName(), MidiSystem.getSequence(new FileInputStream(f)), pitch);
            } catch (InvalidMidiDataException | IOException e) {
                throw new RuntimeException(RESOLVE_ERROR);
            }
        }

        throw new RuntimeException(UNSUPPORTED_FORMAT);
    }


    @QuarkCommand(name = "music", permission = "-quark.music.play")
    public static final class MusicCommand extends ModuleCommand<MusicPlayer> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            RemoteMessageService service = RemoteMessageService.getInstance();

            String operator = sender.getName();

            switch (args[0]) {
                case "cancel" -> {
                    this.getModule().cancelMusic(operator);
                    service.sendBroadcast("/music/control", msg -> BufferUtil.writeString(msg, "cancel;" + operator));
                }
                case "pause" -> {
                    this.getModule().pauseMusic(operator);
                    service.sendBroadcast("/music/control", msg -> BufferUtil.writeString(msg, "pause;" + operator));
                }
                case "resume" -> {
                    this.getModule().resumeMusic(operator);
                    service.sendBroadcast("/music/control", msg -> BufferUtil.writeString(msg, "resume;" + operator));
                }
                case "play" -> {
                    int pitch = 0;

                    ConfigurationSection pitchOffsets = this.getConfig().getConfigurationSection("pitch-offsets");
                    if (pitchOffsets != null && pitchOffsets.contains(args[1])) {
                        pitch = pitchOffsets.getInt(args[1]);
                    }

                    if (args.length > 2) {
                        pitch = Integer.parseInt(args[2]);
                    }

                    String music;

                    if (Objects.equals(args[1], "random")) {
                        music = getModule().getRandomMusicName();
                    } else {
                        music = args[1];
                    }

                    if (!this.getModule().loader.list().contains(music)) {
                        this.getLanguage().sendMessageTo(sender, "not-found");
                        return;
                    }

                    this.getModule().playMusic(operator, music, pitch);
                    int finalPitch = pitch;

                    service.sendBroadcast("/music/control", msg -> {
                        String data = "play;%s;%s;%d".formatted(
                                operator, music, finalPitch
                        );
                        BufferUtil.writeString(msg, data);
                    });
                }
                default -> sendExceptionMessage(sender);
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("play");
                tabList.add("cancel");
                tabList.add("pause");
                tabList.add("resume");
            }
            if (buffer.length == 2 && Objects.equals(buffer[0], "play")) {
                tabList.addAll(this.getModule().loader.list());
            }
            if (buffer.length == 3 && Objects.equals(buffer[0], "play")) {
                tabList.add("[pitch]");
            }
        }
    }
}
