package org.atcraftmc.quark.contents.music;

import me.gb2022.simpnet.util.BufferUtil;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.data.assets.AssetGroup;
import org.atcraftmc.starlight.core.RemoteMessageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface MusicFileLoader {
    File load(String name);

    Set<String> list();

    default String random() {
        var music = list();
        var index = SharedObjects.RANDOM.nextInt(music.size());
        return music.toArray(new String[0])[index];
    }

    default int trim() {
        return 0;
    }

    class FileStorage {
        private final String path;

        public FileStorage(String path) {
            this.path = path;
        }

        private File getMD5StorageFile() {
            return new File(this.path + "/_checksums.md5.dat");
        }

        
    }


    class LocalLoader implements MusicFileLoader {
        private final AssetGroup folder;

        public LocalLoader(AssetGroup folder) {
            this.folder = folder;
        }

        @Override
        public File load(String name) {
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
            int count = 0;
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

    class RemoteLoader implements MusicFileLoader {
        private final AssetGroup folder;
        private final String contentServer;

        public RemoteLoader(AssetGroup folder, String contentServer) {
            this.folder = folder;
            this.contentServer = contentServer;
        }

        @Override
        public File load(String name) {
            var file = this.folder.getFile(name);
            var message = RemoteMessageService.instance();

            if (file.exists() && file.length() > 0) {
                return this.folder.getFile(name);
            }

            message.query(this.contentServer, "music:get", (b) -> BufferUtil.writeString(b, name))
                    .timeout(1250, () -> {throw new RuntimeException(MusicPlayer.TIMEOUT);})
                    .result(b -> {
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
                    })
                    .request();

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

        interface FileChecksum {
            static File getMD5File(File file) {
                var path = file.getParentFile().getAbsolutePath() + "/.md5/" + file.getName() + ".md5";
                var checksumFile = new File(path);

                if (checksumFile.exists()) {
                    return checksumFile;
                }

                file.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return checksumFile;
            }

            static String getMD5Value(File file) {
                var md5File = getMD5File(file);

                if (md5File.exists() && md5File.length() > 0) {
                    var path = md5File.getAbsolutePath();

                    try (var stream = Files.newInputStream(Paths.get(path))) {
                        return new String(stream.readAllBytes(), StandardCharsets.US_ASCII);
                    } catch (Exception e) {
                        return "_";
                    }
                }

                var md5 = calculate(file);

                write(file, md5);
                return md5;
            }

            static void write(File file, String md5) {
                var md5File = getMD5File(file);

                try (var out = new FileOutputStream(md5File)) {
                    out.write(md5.getBytes(StandardCharsets.US_ASCII));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            static String calculate(File file) {
                var path = Paths.get(file.getAbsolutePath());
                try (var stream = Files.newInputStream(path)) {
                    var digest = MessageDigest.getInstance("MD5");
                    var buffer = new byte[4096];

                    while (stream.read(buffer) != -1) {
                        digest.update(buffer);
                    }

                    return new String(digest.digest(), StandardCharsets.US_ASCII);
                } catch (Exception e) {
                    return "_";
                }
            }
        }
    }
}
