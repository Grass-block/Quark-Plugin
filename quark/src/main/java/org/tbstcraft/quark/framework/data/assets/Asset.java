package org.tbstcraft.quark.framework.data.assets;

import me.gb2022.commons.file.Files;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.FilePath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public final class Asset {
    public static final String PATH = "%s/assets/%s";
    public static final String TEMPLATE = "/assets/%s";

    private final String name;
    private final File file;
    private final FileLoader loader;
    private final Plugin provider;

    public Asset(Plugin provider, String name, boolean cached) {
        this.provider = provider;
        this.name = name;
        this.file = new File(PATH.formatted(FilePath.pluginFolder(Quark.PLUGIN_ID), name));

        this.loader = cached ? FileLoader.cached(this.file) : FileLoader.direct(this.file);
    }

    public byte[] getData() {
        return this.loader.load();
    }

    public String asText() {
        return new String(this.getData(), StandardCharsets.UTF_8);
    }

    public void asInputStream(Consumer<InputStream> reader) {
        InputStream stream = new ByteArrayInputStream(this.getData());

        reader.accept(stream);

        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getFile() {
        if (!this.file.exists()) {
            this.save();
        }
        return this.file;
    }

    public void save() {
        InputStream stream = this.provider.getClass().getResourceAsStream(TEMPLATE.formatted(this.name));

        if (stream == null) {
            System.out.println(TEMPLATE.formatted(this.name));
            return;
        }

        Files.copy(stream, this.file);
    }

    public String getName() {
        return name;
    }

    public URL asURL() {
        try {
            return this.getFile().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
