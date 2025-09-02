package org.atcraftmc.starlight.migration;

import com.google.common.io.Files;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.util.FilePath;

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

            Starlight.instance().getLogger().info("fixing up folder %s -> %s".formatted(origin, dest));

            File folder = new File(base + dest);
            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    Starlight.instance().getLogger().info("created new folder %s".formatted(dest));
                }
            }

            for (File f : Objects.requireNonNull(legacyFolder.listFiles())) {
                File moved = new File(base + "/" + dest + "/" + f.getName());
                try {
                    Files.copy(f, moved);
                } catch (IOException e) {
                    //Starlight.getInstance().getLogger().warning("failed to move file %s".formatted(f.getName()));
                }
                if (!f.delete()) {
                    //Starlight.getInstance().getLogger().warning("failed to remove file %s".formatted(f.getName()));
                }
            }

            if (legacyFolder.delete()) {
                Starlight.instance().getLogger().info("removed folder %s".formatted(origin));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void move(File origin, File dest) {
        if (origin.isDirectory()) {
            var fs = origin.listFiles();

            if (fs == null) {
                return;
            }

            for (var f : fs) {
                move(f, new File(dest.getAbsolutePath() + File.separator + f.getName()));
            }

            return;
        }

        try {
            Files.copy(origin, dest);
            if (!origin.delete()) {
                //Starlight.getInstance().getLogger().warning("failed to remove file %s".formatted(origin.getName()));
            }
        } catch (Exception e) {
            //Starlight.getInstance().getLogger().warning("failed to move file %s".formatted(origin.getName()));
        }
    }

    static void redirectDataFolder(String origin, String dest) {
        var legacyFolder = new File(FilePath.pluginsFolder() + "/" + origin);
        var destFolder = new File(FilePath.pluginsFolder() + "/" + dest);

        if (!legacyFolder.exists()) {
            return;
        }

        move(legacyFolder, destFolder);
    }

    static void moveFile(String origin, String dest) {
        var base = FilePath.pluginFolder("quark");
        var originFile = new File(base + origin);
        var destFile = new File(base + dest);

        if (!originFile.exists()) {
            return;
        }
        if (destFile.exists() && destFile.length() > 0) {
            return;
        }

        Starlight.instance().getLogger().info("fixing up folder %s -> %s".formatted(origin, dest));

        try {
            Files.copy(originFile, destFile);
        } catch (IOException e) {
            Starlight.instance().getLogger().warning("failed to move file %s".formatted(origin));
        }

        if (originFile.delete()) {
            Starlight.instance().getLogger().info("removed file %s".formatted(dest));
        }
    }
}
