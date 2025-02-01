package org.atcraftmc.quark.contents.music;

import me.gb2022.apm.remote.util.BufferUtil;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.tbstcraft.quark.internal.RemoteMessageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface MusicFileLoader {
    File load(String name);

    Set<String> list();

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
                                Quark.getInstance().getLogger().info("cached music file %s.".formatted(file.getName()));
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
