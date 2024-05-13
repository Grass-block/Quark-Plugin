package org.tbstcraft.quark.contents.musics;

import me.gb2022.apm.remote.protocol.BufferUtil;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.contents.MusicPlayer;
import org.tbstcraft.quark.service.network.RemoteMessageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface MusicFileLoader {
    File load(String name);

    List<String> list();

    class LocalLoader implements MusicFileLoader {
        private final String folder;

        public LocalLoader(String folder) {
            this.folder = folder;
        }

        @Override
        public File load(String name) {
            if (!list().contains(name)) {
                return null;
            }
            return new File(this.folder + "/" + name);
        }

        @Override
        public List<String> list() {
            List<String> lists = new ArrayList<>();

            for (File f : Objects.requireNonNull(new File(this.folder).listFiles())) {
                lists.add(f.getName());
            }

            return lists;
        }
    }

    class RemoteLoader implements MusicFileLoader {
        private final String folder;
        private final String contentServer;

        public RemoteLoader(String folder, String contentServer) {
            this.folder = folder;
            this.contentServer = contentServer;
        }

        @Override
        public File load(String name) {
            File f = new File(this.folder + "/" + name);

            if (!f.exists() || f.length() == 0) {
                RemoteMessageService.getInstance().sendQuery(this.contentServer, "/music/get", (b) -> BufferUtil.writeString(b, name))
                        .timeout(1000, () -> {
                            throw new RuntimeException(MusicPlayer.TIMEOUT);
                        }).result(b -> {
                            try {
                                if (f.createNewFile()) {
                                    Quark.LOGGER.info("cached music file %s.".formatted(f.getName()));
                                }
                                FileOutputStream stream = new FileOutputStream(f);
                                stream.write(BufferUtil.readArray(b));
                                stream.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).sync();
            }

            return new File(this.folder + "/" + name);
        }

        @Override
        public List<String> list() {
            List<String> lists = new ArrayList<>();

            RemoteMessageService.getInstance().sendQuery(contentServer, "/music/list", (b -> {
            })).timeout(5000, () -> {
                System.out.println("?????");
            }).result((b) -> lists.addAll(List.of(BufferUtil.readString(b).split(";")))).sync();
            return lists;
        }
    }
}
