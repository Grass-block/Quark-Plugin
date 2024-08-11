package org.tbstcraft.quark.contents.music;

import me.gb2022.apm.remote.protocol.BufferUtil;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.tbstcraft.quark.internal.RemoteMessageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
            File f = this.folder.getFile(name);

            if (!f.exists() || f.length() == 0) {
                RemoteMessageService.query(this.contentServer, "/music/get", (b) -> BufferUtil.writeString(b, name))
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

            return this.folder.getFile(name);
        }

        @Override
        public Set<String> list() {
            Set<String> lists = new HashSet<>();

            RemoteMessageService.query(contentServer, "/music/list", (b -> {
            })).timeout(5000, () -> {
            }).result((b) -> lists.addAll(List.of(BufferUtil.readString(b).split(";")))).sync();
            return lists;
        }
    }
}
