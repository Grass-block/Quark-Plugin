package org.atcraftmc.quark.selfhost;

import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.framework.module.SLPackageModule;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@ApplicationModule(defaultEnable = false, description = "FRPC service holder")
public final class FrpcHolder extends SLPackageModule {
    private Process process;

    @Override
    public void enable() {
        var cmd = config().value("command").string();
        var path = config().value("path").string();

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
                        this.handle().getLogger().info("[frpc] {}", line);
                    }
                }

                this.process.waitFor();
            } catch (Exception e) {
                this.handle().getLogger().catching(e);
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
