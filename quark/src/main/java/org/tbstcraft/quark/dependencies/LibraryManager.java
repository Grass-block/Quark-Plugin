package org.tbstcraft.quark.dependencies;

import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public interface LibraryManager {
    String CN_MAVEN = "https://maven.aliyun.com/repository/public/central";
    String GLOBAL_MAVEN = "https://repo1.maven.org/maven2/%s";


    static void download(String url) {

    }

    static void load(){

    }

    static void checkSha1(String dependencies) {

    }


    static void downloadFile(File target, String groupId, String artifactId, String version, String ext) {
        var file= target.getParentFile().mkdirs();
        URLConnection connection = null;
        try {
            connection = new URL(CN_MAVEN + String.format("/%1$s/%2$s/%3$s/%2$s-%3$s.%4$s",
                                                          groupId.replace(".", "/"),
                                                          artifactId,
                                                          version,
                                                          ext
                                                         )).openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(120000);
        connection.setUseCaches(true);
        try (InputStream inputStream = connection.getInputStream()) {
            Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    final class LibraryLoader {
        private final Object parentLoaderUCP;
        private final Object loaderUCP;
        private final MethodHandle addURLMethod;
        private final long fieldOffset;
        private final sun.misc.Unsafe unsafe;

        public LibraryLoader(ClassLoader loader) {
            this.unsafe = getUnsafe();

            try {
                Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                MethodHandles.Lookup lookup = (MethodHandles.Lookup) unsafe.getObject(unsafe.staticFieldBase(field),
                                                                                      unsafe.staticFieldOffset(field)
                                                                                     );
                Field ucpField;
                try {
                    ucpField = loader.getClass().getDeclaredField("ucp");
                } catch (NoSuchFieldException e) {
                    ucpField = loader.getClass().getSuperclass().getDeclaredField("ucp");
                }
                fieldOffset = unsafe.objectFieldOffset(ucpField);
                loaderUCP = unsafe.getObject(loader, fieldOffset);
                Method method = loaderUCP.getClass().getDeclaredMethod("addURL", URL.class);
                addURLMethod = lookup.unreflect(method);
                if (loader.getParent() != null) {
                    parentLoaderUCP = unsafe.getObject(loader.getParent(), fieldOffset);
                } else {
                    parentLoaderUCP = null;
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        private static Unsafe getUnsafe() {
            try {
                return Unsafe.getUnsafe();
            } catch (Exception e) {
                try {
                    Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (sun.misc.Unsafe) theUnsafe.get(null);
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
        }


        private void loadUCP(File file, Object ucp) {
            try {
                this.addURLMethod.invoke(ucp, file.toURI().toURL());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        private void load(File file) {
            loadUCP(file, this.loaderUCP);
        }

        private void loadParent(File file) {
            if (this.parentLoaderUCP == null) {
                throw new IllegalStateException("parentLoaderUCP is null");
            }
            loadUCP(file, this.parentLoaderUCP);
        }

        private void loadWithParent(File file) {
            load(file);
            loadParent(file);
        }

        private void loadCL(File file, ClassLoader loader) {
            var obj = this.unsafe.getObject(loader, this.fieldOffset);
            loadUCP(file, obj);
        }

    }
}
