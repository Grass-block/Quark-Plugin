package org.atcraftmc.starlight.util.dependency;

/**
 * Gradle依赖坐标类
 */
public final class GradleDependency {
    private String group;
    private String name;
    private String version;

    public GradleDependency(String group, String name, String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    public static GradleDependency fromGradle(String gradle) {
        var args = gradle.split(":");
        return new GradleDependency(args[0], args[1], args[2]);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return group + ":" + name + ":" + version;
    }

    public String toMavenPath() {
        return group.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + ".jar";
    }

    public String toPomPath() {
        return group.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + ".pom";
    }

    public String toFlatPomPath() {
        return group + "_" + name + "_" + version + "_" + name + "-" + version + ".pom";
    }

    public String toFlatFilePath() {
        return "%s_%s_%s.jar".formatted(this.group, this.name, this.version);
    }
}
