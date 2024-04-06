package org.tbstcraft.quark.contents;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.task.TaskService;
import org.tbstcraft.quark.util.ExceptionUtil;
import org.tbstcraft.quark.util.FilePath;

import javax.sound.midi.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@CommandRegistry(MidiNodeBlockPlayer.MidiPlayCommand.class)
@QuarkModule(version = "1.0.3")
public class MidiNodeBlockPlayer extends PackageModule {
    public static final String UNSUPPORTED_FORMAT = "unsupported-format";
    public static final String RESOLVE_ERROR = "error-resolving";
    public static final String NOT_FOUND = "not-found";

    private final Map<String, MidiSession> sessions = new HashMap<>();

    private Thread currentPlayThread;
    private boolean playing = false;


    @Override
    public void enable() {
        getFolder();

        release("avicii-Waiting_For_Love.mid");
        release("Beyond-海阔天空.mid");
    }

    private void release(String name) {
        FilePath.tryReleaseAndGetFile("/assets/%s".formatted(name), getFolder().getAbsolutePath() + "/%s".formatted(name));
    }

    private File getFolder() {
        File file = new File(FilePath.pluginFolder(Quark.PLUGIN_ID) + "/midi/");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    @Override
    public void disable() {
        for (String s : new HashSet<>(TaskService.getAllTasks().keySet())) {
            if (s.startsWith("quark_midi")) {
                TaskService.cancelTask(s);
            }
        }
        this.stopThread();
    }

    private void play(Sequence sequence, int offset) {
        this.currentPlayThread = new Thread(() -> {
            try {
                new MidiChannel(sequence, offset).play((node, off) -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        playNode(p, node.node, (int) off);
                    }
                });
                this.currentPlayThread = null;
                this.playing = false;
            } catch (Throwable ignored) {
                this.currentPlayThread = null;
                this.playing = false;
            }
        });
        this.currentPlayThread.start();
        this.playing = true;
    }

    private void playNode(Player player, int node, int off) {
        int base = node - 23 + off - 6;
        if (base < 0 || base >= 72) {
            return;
        }

        Instrument instrument = base < 24 ? Instrument.BASS_GUITAR : base >= 48 ? Instrument.BELL : Instrument.PIANO;

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

    private void stopThread() {
        if (this.currentPlayThread != null) {
            this.currentPlayThread.interrupt();
            this.currentPlayThread = null;
        }
    }

    private MusicData select(String action, int pitch) {
        String file;

        if (action.equals("random")) {
            List<String> tabList = new ArrayList<>();
            for (File f : Objects.requireNonNull(this.getFolder().listFiles())) {
                tabList.add(f.getName());
            }
            file = tabList.get(new Random().nextInt(tabList.size()));
        } else {
            file = action;
        }

        File f = new File(this.getFolder().getAbsolutePath() + "/" + file);

        if (!f.exists()) {
            throw new IllegalArgumentException(NOT_FOUND);
        }

        if (file.endsWith(".mid") || file.endsWith(".midi")) {
            try {
                return MusicData.fromMidi(MidiSystem.getSequence(new FileInputStream(f)), pitch);
            } catch (InvalidMidiDataException | IOException e) {
                throw new RuntimeException(RESOLVE_ERROR);
            }
        }

        throw new RuntimeException(UNSUPPORTED_FORMAT);
    }

    @Deprecated
    @FunctionalInterface
    private interface PlayerCallback {
        void play(MidiNode node, long off);
    }

    @QuarkCommand(name = "midi", permission = "-quark.midi.play")
    public static final class MidiPlayCommand extends ModuleCommand<MidiNodeBlockPlayer> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args[0].equals("stop")) {
                this.getModule().stopThread();
                this.getLanguage().sendMessageTo(sender, "interrupt");
                return;
            }
            if (args[0].equals("random")) {
                List<String> tabList = new ArrayList<>();
                for (File f : Objects.requireNonNull(this.getModule().getFolder().listFiles())) {
                    tabList.add(f.getName());
                }
                args[0] = tabList.get(new Random().nextInt(tabList.size()));
            }

            if (this.getModule().playing) {
                this.getLanguage().sendMessageTo(sender, "interrupt-hint");
                return;
            }

            File f = new File(this.getModule().getFolder().getAbsolutePath() + "/" + args[0]);

            if (!f.exists()) {
                this.getLanguage().sendMessageTo(sender, "not-found", args[0]);
                return;
            }

            int pitch = 0;

            ConfigurationSection pitchOffsets = this.getConfig().getConfigurationSection("pitch-offsets");
            if (pitchOffsets != null && pitchOffsets.contains(args[0])) {
                pitch = pitchOffsets.getInt(args[0]);
            }

            if (args.length > 1) {
                pitch = Integer.parseInt(args[1]);
            }

            try {
                this.getModule().play(MidiSystem.getSequence(new FileInputStream(f)), pitch);
            } catch (InvalidMidiDataException | IOException e) {
                throw new RuntimeException(e);
            }


            this.getLanguage().broadcastMessage(false, "play", args[0], pitch);
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                for (File f : Objects.requireNonNull(this.getModule().getFolder().listFiles())) {
                    tabList.add(f.getName());
                }
                tabList.add("stop");
                tabList.add("random");
            }
            if (buffer.length == 2) {
                tabList.add("[pitch]");
            }
        }
    }

    @QuarkCommand(name = "play-all")
    public static final class MidiPlayAllCommand extends ModuleCommand<MidiNodeBlockPlayer> {

    }

    @Deprecated
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class MidiChannel {
        private final List[] chn;
        private final Sequence sequence;
        private final int offset;

        public MidiChannel(Sequence sequence, int offset) {
            this.sequence = sequence;
            chn = new List[(int) sequence.getTickLength()];
            this.offset = offset;
            Track[] tracks = sequence.getTracks();
            for (Track track : tracks) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    MidiMessage message = event.getMessage();
                    if (!(message instanceof ShortMessage sm)) {
                        continue;
                    }

                    if (sm.getCommand() != ShortMessage.NOTE_ON) {
                        continue;
                    }
                    int note = sm.getData1();
                    int velocity = sm.getData2();
                    this.addNode((int) event.getTick(), new MidiNode(note, velocity));
                }
            }
        }

        public void addNode(int position, MidiNode node) {
            if (chn[position] == null) {
                chn[position] = new ArrayList<>();
            }
            chn[position].add(node);
        }

        public void play(PlayerCallback callback) {
            int delayedTicks = 0;
            for (List<MidiNode> section : this.chn) {
                if (section == null) {
                    delayedTicks++;
                    continue;
                }
                long delayMilliseconds = sequence.getMicrosecondLength() * delayedTicks / sequence.getTickLength() / 1000;
                try {
                    Thread.sleep(delayMilliseconds);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                delayedTicks = 0;
                for (MidiNode node : section) {
                    callback.play(node, this.offset);
                }
            }
        }
    }

    @Deprecated
    private static final class MidiNode {
        private final int node;
        private final float velocity;

        private MidiNode(int node, int velocity) {
            this.node = node;
            this.velocity = velocity;
        }
    }


    @QuarkCommand(name = "midi",permission = "+quark.midi")
    public static final class MidiCommand extends ModuleCommand<MidiNodeBlockPlayer>{
        //midi play xxx 0 -all
        //midi pause -all
    }



    @SuppressWarnings("unchecked")
    private static final class MusicData {
        private final List<MusicNode>[] nodes;
        private final int globalNodeOffset;
        private final long tickLength;
        private final long millsLength;

        private MusicData(int globalNodeOffset, long tickLength, long millsLength) {
            this.globalNodeOffset = globalNodeOffset;
            this.tickLength = tickLength;
            this.millsLength = millsLength;

            this.nodes = new List[(int) tickLength];
        }

        static MusicData fromMidi(Sequence sequence, int offset) {
            MusicData data = new MusicData(offset, sequence.getTickLength(), sequence.getMicrosecondLength());

            Map<Track, EnumInstrument> channelInstrumentCache = new HashMap<>();

            Track[] tracks = sequence.getTracks();
            for (Track track : tracks) {
                channelInstrumentCache.put(track, EnumInstrument.PIANO);

                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    MidiMessage message = event.getMessage();
                    if (!(message instanceof ShortMessage sm)) {
                        continue;
                    }

                    switch (sm.getCommand()) {
                        case ShortMessage.NOTE_ON -> {
                            int note = sm.getData1();
                            int velocity = sm.getData2();
                            EnumInstrument instrument = channelInstrumentCache.get(track);
                            data.addNode((int) event.getTick(), new MusicNode(note, velocity, instrument));
                        }

                        case ShortMessage.PROGRAM_CHANGE -> {

                        }
                    }
                }
            }

            return data;
        }

        public void addNode(int tick, MusicNode node) {
            if (this.nodes[tick] == null) {
                this.nodes[tick] = new ArrayList<>();
            }
            this.nodes[tick].add(node);
        }

        public void play(MusicPlayer player) {

        }

        enum EnumInstrument {
            PIANO
        }

        interface MusicPlayer {
            void play(MusicNode node, long offset);
        }

        public static final class MusicNode {
            private final int node;
            private final float power;
            private final EnumInstrument instrument;

            public MusicNode(int node, float power, EnumInstrument instruments) {
                this.node = node;
                this.power = power;
                this.instrument = instruments;
            }

            public EnumInstrument getInstrument() {
                return instrument;
            }

            public float getPower() {
                return power;
            }

            public int getNode() {
                return node;
            }
        }
    }

    private static final class MidiSession implements Runnable {
        private final Queue<MusicData> musicQueue = new ArrayDeque<>();
        private MusicData next;

        private boolean pause = false;
        private boolean cancel = true;

        private boolean sessionRunning = true;

        //thread
        @Override
        public void run() {
            while (this.sessionRunning) {
                if (this.musicQueue.isEmpty()) {
                    try {
                        Thread.sleep(100);
                        Thread.yield();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                MusicData current = this.next;
                this.next = this.musicQueue.poll();
                this.playSelected(current);
            }

            this.musicQueue.clear();
            this.cancel();
        }

        private void playSelected(MusicData current) {
            int currentTick = -1;
            int delayedTicks = 0;
            while (currentTick < current.tickLength) {
                if (this.cancel) {
                    this.cancel = false;
                    return;
                }

                while (this.pause) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Thread.yield();
                }

                currentTick++;
                if (current.nodes[currentTick] == null) {
                    delayedTicks++;
                    continue;
                }

                long delayMilliseconds = current.millsLength * delayedTicks / current.tickLength / 1000;
                try {
                    Thread.sleep(delayMilliseconds);
                } catch (InterruptedException e) {
                    ExceptionUtil.log(e);
                }
                delayedTicks = 0;
                for (MusicData.MusicNode node : current.nodes[currentTick]) {


                    //TODO play note
                }
            }
        }

        public void stopSession() {
            this.sessionRunning = false;
        }


        //control
        private void playNow(MusicData data) {
            this.next = data;
            this.cancel();
        }

        public void pause() {
            this.pause = true;
        }

        public void resume() {
            this.pause = false;
        }

        public void cancel() {
            this.cancel = true;
        }
    }
}
