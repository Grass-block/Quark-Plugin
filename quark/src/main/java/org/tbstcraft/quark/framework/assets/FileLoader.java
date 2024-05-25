package org.tbstcraft.quark.framework.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class FileLoader {
    private final File file;

    protected FileLoader(File file) {
        this.file = file;
    }

    public static FileLoader direct(File f) {
        return new DirectLoader(f);
    }

    public static FileLoader cached(File f) {
        return new CachedLoader(f);
    }

    public abstract byte[] load();

    public File getFile() {
        return file;
    }

    public static final class DirectLoader extends FileLoader {
        protected DirectLoader(File file) {
            super(file);
        }

        @Override
        public byte[] load() {
            try (InputStream stream = new FileInputStream(this.getFile())) {
                return stream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final class CachedLoader extends FileLoader {
        private byte[] data;

        protected CachedLoader(File file) {
            super(file);
        }

        @Override
        public byte[] load() {
            if (this.data != null) {
                return this.data;
            }

            try {
                InputStream stream = new FileInputStream(this.getFile());
                this.data = stream.readAllBytes();
                stream.close();

                return this.data;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
