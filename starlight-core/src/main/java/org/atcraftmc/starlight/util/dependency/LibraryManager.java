package org.atcraftmc.starlight.util.dependency;

import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.starlight.Starlight;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class LibraryManager {
    public static final Logger LOGGER = Starlight.LOGGER;

    private final String repositoryURL;
    private final String workingDirectory;
    private final Map<String, URL> loadedURLs = new HashMap<>();

    public LibraryManager(String repositoryURL, String workingDirectory) {
        this.repositoryURL = repositoryURL.replace("https://", "").replace("http://", "");
        this.workingDirectory = workingDirectory;
    }

    public String getPOMDocument(GradleDependency dependency) throws Exception {
        var pf = new File(this.workingDirectory + "/maven-poms/" + dependency.toFlatPomPath());

        if (!pf.exists() || pf.length() == 0) {
            String pomUrl = this.repositoryURL + dependency.toPomPath();
            if (pf.getParentFile().mkdirs()) {
                LOGGER.info("created pom dir {}", pf.getParentFile().getAbsolutePath());
            }
            if (!pf.createNewFile()) {
                LOGGER.error("failed to create pom file {}", pf.getAbsolutePath());
            }

            var conn = HttpRequest.https(HttpMethod.GET, pomUrl).build().createConnection();
            try {
                var code = conn.getResponseCode();

                if (code != 200) {
                    throw new IOException("Failed to download POM: " + pomUrl + ", status: " + conn.getContent());
                }

                try (var in = conn.getInputStream(); var out = new FileOutputStream(pf)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                    conn.disconnect();
                }
            } catch (Exception e) {
                conn.disconnect();
                throw e;
            }
        }

        try (var in = new FileInputStream(pf)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public File getDependencyFile(GradleDependency dependency) throws Exception {
        var pointer = new File(this.workingDirectory + "/maven-libraries/" + dependency.toFlatFilePath());

        if (pointer.exists() && pointer.length() > 0) {
            return pointer;
        }

        if (pointer.getParentFile().mkdirs()) {
            LOGGER.info("created jar dir {}", pointer.getParentFile().getAbsolutePath());
        }
        if (!pointer.createNewFile()) {
            LOGGER.error("failed to create jar file {}", pointer.getAbsolutePath());
        }

        var url = this.repositoryURL + dependency.toMavenPath();
        var conn = HttpRequest.https(HttpMethod.GET, url).build().createConnection();

        try {
            var code = conn.getResponseCode();

            if (code != 200) {
                throw new IOException("Failed to download: " + url + ", status: " + conn.getResponseMessage());
            }

            try (var in = conn.getInputStream(); var out = new FileOutputStream(pointer)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            LOGGER.info("Downloaded {} -> {} ({}KB)", dependency.toString(), url, pointer.length() / 1024);
            conn.disconnect();

            return pointer;
        } catch (Exception e) {
            LOGGER.catching(e);
            conn.disconnect();
            throw e;
        }
    }

    public void resolveDependencies(Collection<GradleDependency> dependencies) {
        var v = new ArrayList<String>();

        for (var dep : dependencies) {
            try {
                checkDependencyFully(dep, v);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<GradleDependency> resolvePOMDependencies(GradleDependency dependency) throws Exception {
        return getPOMDependencies(getPOMDocument(dependency));
    }

    public void clearCache() {
        delete(Paths.get(this.workingDirectory + "/maven-poms"));
        delete(Paths.get(this.workingDirectory + "/maven-libraries"));
    }

    public void injectLibraries(Object context) {
        var cl = (URLClassLoader) context.getClass().getClassLoader();
        for (var url : this.loadedURLs.values()) {
            try {
                var addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                try {
                    addURLMethod.setAccessible(true);
                } catch (Exception e) {
                    var c_unsafe = Class.forName("sun.misc.Unsafe");
                    var f_unsafe = c_unsafe.getDeclaredField("theUnsafe");

                    f_unsafe.setAccessible(true);

                    var unsafe = (Unsafe) f_unsafe.get(null);
                    var baseModule = Object.class.getModule();
                    var c_current = LibraryManager.class; //todo: use ref if error
                    var addr = unsafe.objectFieldOffset(Class.class.getDeclaredField("module"));
                    var prev = unsafe.getAndSetObject(c_current, addr, baseModule);

                    addURLMethod.setAccessible(true);
                    unsafe.getAndSetObject(c_current, addr, prev);
                }
                addURLMethod.invoke(cl, url);
                LOGGER.info("injected dependency: {}", new File(url.toURI()).getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }


    private void checkDependencyFully(GradleDependency dependency, List<String> visited) throws Exception {
        var dependencyKey = dependency.toString();
        if (visited.contains(dependencyKey)) {
            return;
        }
        visited.add(dependencyKey);

        var file = getDependencyFile(dependency);
        this.loadedURLs.put(dependencyKey, file.toURI().toURL());

        try {
            var parents = resolvePOMDependencies(dependency);
            for (var parent : parents) {
                checkDependencyFully(parent, visited);
            }
        } catch (Exception e) {
            LOGGER.catching(e);
            e.printStackTrace();
        }
    }

    private List<GradleDependency> getPOMDependencies(String pomContent) {
        List<GradleDependency> dependencies = new ArrayList<>();

        // 简单的字符串解析（实际项目中应该使用DOM或SAX解析器）
        String[] lines = pomContent.split("\n");
        boolean inDependenciesSection = false;

        for (String line : lines) {
            line = line.trim();

            if (line.contains("<dependencies>")) {
                inDependenciesSection = true;
                continue;
            }

            if (line.contains("</dependencies>")) {
                break;
            }

            if (inDependenciesSection && line.contains("<dependency>")) {
                var dep = dispatchPOMDependencies(lines, pomContent.indexOf(line));
                if (dep != null && !dep.getVersion().isEmpty()) {
                    dependencies.add(dep);
                }
            }
        }

        return dependencies;
    }

    private GradleDependency dispatchPOMDependencies(String[] lines, int startIndex) {
        var groupId = "";
        var artifactId = "";
        var version = "";
        var scope = "compile";

        for (int i = startIndex; i < lines.length; i++) {
            var line = lines[i].trim();

            if (line.contains("</dependency>")) {
                break;
            }

            if (line.contains("<groupId>")) {
                groupId = extractXmlValue(line, "groupId");
            } else if (line.contains("<artifactId>")) {
                artifactId = extractXmlValue(line, "artifactId");
            } else if (line.contains("<version>")) {
                version = extractXmlValue(line, "version");
            } else if (line.contains("<scope>")) {
                scope = extractXmlValue(line, "scope");
            }
        }

        // 只处理compile和runtime范围的依赖
        if ("compile".equals(scope) || "runtime".equals(scope) || scope.isEmpty()) {
            return new GradleDependency(groupId, artifactId, version);
        }

        return null;
    }

    private String extractXmlValue(String line, String tagName) {
        int start = line.indexOf("<" + tagName + ">") + tagName.length() + 2;
        int end = line.indexOf("</" + tagName + ">");
        if (start > 0 && end > start) {
            return line.substring(start, end).trim();
        }
        return "";
    }

    private void delete(Path libPath) {
        if (!Files.exists(libPath)) {
            return;
        }

        try (var s = Files.walk(libPath)) {
            s.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    System.err.println("Failed to delete: " + path);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}