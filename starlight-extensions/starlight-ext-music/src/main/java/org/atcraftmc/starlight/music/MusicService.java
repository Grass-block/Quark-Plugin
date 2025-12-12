package org.atcraftmc.starlight.music;

import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO3;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.ServiceHolder;
import me.gb2022.modular.service.injection.ServiceInject;
import me.gb2022.modular.service.injection.ServiceProvider;
import me.gb2022.simpnet.util.BufferUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.RemoteMessageService;
import org.atcraftmc.starlight.data.assets.AssetGroup;
import org.atcraftmc.starlight.framework.SLService;
import org.atcraftmc.starlight.migration.QuarkDataImporter;
import org.atcraftmc.starlight.music.resolve.EnumInstrument;
import org.atcraftmc.starlight.music.resolve.MusicData;
import org.atcraftmc.starlight.music.resolve.MusicResolveRequest;
import org.atcraftmc.starlight.music.resolve.MusicResolver;
import org.atcraftmc.starlight.util.FilePath;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ApplicationService(id = "music-service")
public interface MusicService extends SLService {
    String UNSUPPORTED_FORMAT = "unsupported-format";
    String RESOLVE_ERROR = "error-resolving";
    String NOT_FOUND = "not-found";
    String TIMEOUT = "timeout";

    @ServiceInject
    ServiceHolder<MusicService> INSTANCE = new ServiceHolder<>();

    MethodHandleO3<Player, Sound, Float, Float> PLAY_NOTE = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> {
            Class.forName("org.bukkit.SoundCategory");
            return null;
        }, (p, s, power, pitch) -> p.playSound(p.getLocation(), s, SoundCategory.RECORDS, power, pitch));
        ctx.dummy((p, s, power, pitch) -> p.playSound(p.getLocation(), s, power, pitch));
    });

    @ServiceProvider
    static MusicService create(ConfigEntry config) {
        var remote = config.value("remote").bool();
        var group = new AssetGroup(Starlight.instance(), "music", true);

        if (remote) {
            return new RemoteLoader(group, config.value("cdn-server").string());
        }
        return new LocalLoader(group);
    }

    static MusicService instance() {
        return INSTANCE.get();
    }

    static void playNode(Set<Player> audience, int node, int off, EnumInstrument targetInstrument, float power) {
        int base = node - 23 + off - 6;//wtf
        if (base < 0 || base >= 72) {
            return;
        }

        var remapped = switch (targetInstrument) {
            case GUITAR ->
                    base > 0 ? base < 24 ? EnumInstrument.BASS_GUITAR : base >= 48 ? EnumInstrument.XYLOPHONE : EnumInstrument.GUITAR : EnumInstrument.BASS_DRUM;
            case PIANO ->
                    base > 0 ? base < 24 ? EnumInstrument.BASS_GUITAR : base >= 48 ? EnumInstrument.BELL : EnumInstrument.PIANO : EnumInstrument.BASS_DRUM;
            case PLING ->
                    base > 0 ? base < 24 ? EnumInstrument.BASS_GUITAR : base >= 48 ? EnumInstrument.BELL : EnumInstrument.PLING : EnumInstrument.BASS_DRUM;
            case BIT ->
                    base > 0 ? base < 24 ? EnumInstrument.DIDGERIDOO : base >= 48 ? EnumInstrument.COW_BELL : EnumInstrument.BIT : EnumInstrument.BASS_DRUM;

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

    default String random() {
        var music = list();
        var index = SharedObjects.RANDOM.nextInt(music.size());
        return music.toArray(new String[0])[index];
    }

    default MusicData select(String name, int pitch, boolean dispatchInstrument, float speedMod, int interpolation) {
        var f = loadFile(name);

        if (f == null || !f.exists()) {
            throw new IllegalArgumentException(NOT_FOUND);
        }

        return MusicResolver.resolve(f, pitch, dispatchInstrument, speedMod, interpolation);
    }

    File loadFile(String name) throws IllegalArgumentException;

    Set<String> list();

    AssetGroup folder();

    default void saveDefaults() {
    }

    default int trim() {
        return 0;
    }

    default MusicData dispatch(MusicResolveRequest request) {
        return select(request.music(), request.pitch(), request.dispatchInstrument(), request.speedMod(), request.interpolation());
    }

    abstract class AbstractService implements MusicService {
        protected final Logger logger = LogManager.getLogger("Starlight:MusicService");
        protected final AssetGroup folder;

        public AbstractService(AssetGroup folder) {
            this.folder = folder;

            if (!this.folder.existFolder()) {
                this.saveDefaults();
            }
        }

        @Override
        public final AssetGroup folder() {
            return folder;
        }
    }

    class LocalLoader extends AbstractService {
        public LocalLoader(AssetGroup folder) {
            super(folder);

            if (!this.folder.existFolder()) {
                this.saveDefaults();
            }
        }

        @Override
        public void enable() {
            QuarkDataImporter.registerCustomDataHandler("starlight:music", (path) -> {
                var folder = new File(path + "/assets/music");
                var dest = new File(FilePath.slDataFolder() + "/assets/music");

                if (!dest.exists()) {
                    if (!dest.mkdirs()) {
                        this.logger.error("failed to create folder: {}", dest.getAbsolutePath());
                        return;
                    }
                }

                for (var f : Objects.requireNonNull(folder.listFiles())) {
                    var file = new File(dest.getAbsolutePath() + "/" + f.getName());

                    if (file.exists() && file.length() > 0) {
                        this.logger.warn("skipped existing file: {}", file.getName());
                        continue;
                    }

                    try {
                        if (!file.createNewFile()) {
                            this.logger.error("failed to create file: {}", file.getName());
                            continue;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    try (var in = new FileInputStream(f); var out = new FileOutputStream(file)) {
                        in.transferTo(out);
                        this.logger.info("created music file: {}", file.getName());
                    } catch (IOException e) {
                        this.logger.catching(e);
                    }
                }
            });
        }

        @Override
        public void saveDefaults() {
            var musicGroup = folder();

            musicGroup.save("Avicii-The_Days.mid");
            musicGroup.save("Avicii-The_Nights.mid");
            musicGroup.save("Avicii-Waiting_For_Love.mid");
            musicGroup.save("Kiss_The_Rain.mid");
            musicGroup.save("Beyond-海阔天空.mid");
            musicGroup.save("HOYO-MiX_-_何者.mid");
            musicGroup.save("HOYO-MiX_-_在银河中孤独摇摆.mid");
            musicGroup.save("HOYO-MiX_-_BusyDayInMonstat.nbs");
            musicGroup.save("HOYO-MiX_-_DayInMonstat.nbs");
            musicGroup.save("HOYO-MiX_-_希望有羽毛和翅膀.nbs");
            musicGroup.save("FripSide_-_only_my_railgun.nbs");
            musicGroup.save("[BA]ConstantModerato.nbs");
            musicGroup.save("买辣椒也用券_-_起风了.mid");
        }

        @Override
        public File loadFile(String name) throws IllegalArgumentException {
            if (!list().contains(name)) {
                return null;
            }
            return this.folder.getFile(name);
        }

        @Override
        public Set<String> list() {
            return this.folder.list();
        }

        @Override
        public int trim() {
            var count = 0;
            for (var file : Objects.requireNonNull(this.folder.getFolder().listFiles())) {
                var name = file.getName();
                if (name.contains(" ")) {
                    if (file.renameTo(new File(this.folder.getFolder(), name.replace(" ", "_")))) {
                        count++;
                    }
                }
            }

            return count;
        }
    }

    class RemoteLoader extends AbstractService {

        private final String contentServer;

        public RemoteLoader(AssetGroup folder, String contentServer) {
            super(folder);
            this.contentServer = contentServer;
        }

        @Override
        public File loadFile(String name) {
            var file = this.folder.getFile(name);
            var message = RemoteMessageService.instance();

            if (file.exists() && file.length() > 0) {
                return this.folder.getFile(name);
            }

            message.query(this.contentServer, "music:get", (b) -> BufferUtil.writeString(b, name)).timeout(1250, () -> {
                throw new RuntimeException(MusicService.TIMEOUT);
            }).result(b -> {
                var buffer = BufferUtil.readArray(b);

                try {
                    if (file.createNewFile()) {
                        Starlight.instance().getLogger().info("cached music file %s.".formatted(file.getName()));
                    }

                    var stream = new FileOutputStream(file);
                    stream.write(buffer);
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).request();

            return this.folder.getFile(name);
        }

        @Override
        public Set<String> list() {
            var lists = new HashSet<String>();

            RemoteMessageService.instance()
                    .query(this.contentServer, "music:list", "")
                    .result((s) -> lists.addAll(List.of(s.split(";"))))
                    .timeout(250L, RemoteMessageService.EMPTY_ACTION)
                    .request();

            return lists;
        }
    }
}
