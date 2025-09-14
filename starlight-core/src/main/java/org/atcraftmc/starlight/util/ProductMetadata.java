package org.atcraftmc.starlight.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.atcraftmc.starlight.util.dependency.GradleDependency;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class ProductMetadata {
    private final String version;
    private final String buildTime;
    private final int apiVersion;
    private final Set<GradleDependency> dependencies;

    public ProductMetadata(String version, String buildTime, int apiVersion, Set<GradleDependency> dependencies) {
        this.version = version;
        this.buildTime = buildTime;
        this.apiVersion = apiVersion;
        this.dependencies = dependencies;
    }

    public ProductMetadata(JsonObject json) {
        this.version = json.get("version").getAsString();
        this.buildTime = json.get("build-time").getAsString();
        this.apiVersion = json.get("api-version").getAsInt();
        this.dependencies = new HashSet<>();

        for (var s : json.getAsJsonArray("libraries")) {
            this.dependencies.add(GradleDependency.fromGradle(s.getAsString()));
        }
    }

    public static ProductMetadata createFromResource(JavaPlugin plugin) {
        var res = plugin.getResource("product-meta.json");
        if (res == null) {
            throw new RuntimeException("Failed to load product meta!");
        }

        try {
            return new ProductMetadata(JsonParser.parseString(new String(res.readAllBytes())).getAsJsonObject());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getVersion() {
        return version;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public Set<GradleDependency> getDependencies() {
        return dependencies;
    }
}
