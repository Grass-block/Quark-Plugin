package org.atcraftmc.quark.utilities;

import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.TaskService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@SLModule(defaultEnable = false, description = "FRPC service holder")
public final class FrpcHolder extends PackageModule {
    private Process process;

    @Override
    public void enable() {
        var cmd = getConfig().value("command").string();
        var path = getConfig().value("path").string();

        try {
            this.process = new ProcessBuilder().directory(new File(path)).command(cmd).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TaskService.async().run("quark:frpc:daemon", () -> {
            try {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(this.process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        getL4jLogger().info("[frpc] {}", line);
                    }
                }

                this.process.waitFor();
            } catch (Exception e) {
                getL4jLogger().catching(e);
            }
            TaskService.async().delay(20, this::enable);
        });
    }

    @Override
    public void disable() {
        TaskService.async().cancel("quark:frpc:daemon");
        this.process.destroyForcibly();
    }
}
