package org.tbstcraft.quark.util;

import com.google.common.io.Files;
import org.tbstcraft.quark.Quark;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("DuplicatedCode")
public interface DataFix {
    static void moveFolder(String origin, String dest) {
        try {
            String base = FilePath.pluginFolder("quark");

            File legacyFolder = new File(base + origin);
            if (!legacyFolder.exists()) {
                return;
            }

            Quark.getInstance().getLogger().info("fixing up folder %s -> %s".formatted(origin, dest));

            File folder = new File(base + dest);
            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    Quark.getInstance().getLogger().info("created new folder %s".formatted(dest));
                }
            }

            for (File f : Objects.requireNonNull(legacyFolder.listFiles())) {
                File moved = new File(base + "/" + dest + "/" + f.getName());
                try {
                    Files.copy(f, moved);
                } catch (IOException e) {
                    Quark.getInstance().getLogger().warning("failed to move file %s".formatted(f.getName()));
                }
                if (!f.delete()) {
                    Quark.getInstance().getLogger().warning("failed to remove file %s".formatted(f.getName()));
                }
            }

            if (legacyFolder.delete()) {
                Quark.getInstance().getLogger().info("removed folder %s".formatted(origin));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void moveFile(String origin, String dest) {
        String base = FilePath.pluginFolder("quark");
        File org = new File(base + origin);

        if (!org.exists()) {
            return;
        }

        Quark.getInstance().getLogger().info("fixing up folder %s -> %s".formatted(origin, dest));

        File des = new File(base + dest);

        try {
            Files.copy(org, des);
        } catch (IOException e) {
            Quark.getInstance().getLogger().warning("failed to move file %s".formatted(origin));
        }

        if (org.delete()) {
            Quark.getInstance().getLogger().info("removed file %s".formatted(dest));
        }
    }
}
