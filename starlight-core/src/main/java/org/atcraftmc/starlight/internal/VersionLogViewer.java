package org.atcraftmc.starlight.internal;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.atcraftmc.starlight.ProductInfo;
import org.atcraftmc.starlight.foundation.command.CoreCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.foundation.command.StarlightCommandManager;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SLModule(id = "version-log-viewer")
public final class VersionLogViewer extends PackageModule implements PluginCommandExecutor {
    private final Map<String, List<String>> versions = new HashMap<>();

    @Override
    public void enable() {
        StarlightCommandManager.getInstance().getCommand("starlight").registerSubCommand(new VersionLogCommand(this));

        String v = "_";
        try (InputStream stream = this.getResource("/update-log.md")) {
            for (String line : new String(stream.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (line.startsWith("###")) {
                    v = line.replace("### ", "").trim();
                    this.versions.put(v, new ArrayList<>());
                    continue;
                }
                if (v.equals("_")) {
                    continue;
                }

                this.versions.get(v).add(line.replace("- ", ChatColor.RED + "  > " + ChatColor.WHITE));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String version = (args.length == 0 ? ProductInfo.version() : args[0]).trim();

        List<String> info = this.versions.get(version);

        if (info == null) {
            this.getLanguage().item("not-found").send(sender,version);
            return;
        }

        this.getLanguage().item("view").send(sender,version);
        for (String s : info) {
            sender.sendMessage(s);
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.addAll(this.versions.keySet());
        }
    }

    @QuarkCommand(name = "view-update")
    public static final class VersionLogCommand extends CoreCommand {
        public VersionLogCommand(VersionLogViewer module) {
            setExecutor(module);
        }
    }
}
