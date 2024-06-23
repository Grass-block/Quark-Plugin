package org.tbstcraft.quark.internal;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.ProductInfo;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkModule(id = "version-log-viewer")
public final class VersionLogViewer extends PackageModule {
    @Override
    public void enable() {
        CommandManager.getQuarkCommand("quark").registerSubCommand(new VersionLogCommand(this));
    }

    @QuarkCommand(name = "view-update")
    public static final class VersionLogCommand extends CoreCommand {
        private final VersionLogViewer module;
        private final Map<String, List<String>> versions = new HashMap<>();

        public VersionLogCommand(VersionLogViewer module) {
            this.module = module;
            String v = "_";
            try (InputStream stream = this.module.getResource("/update-log.md")) {
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
                this.module.getLanguage().sendMessage(sender, "not-found", version);
                return;
            }

            this.module.getLanguage().sendMessage(sender, "view", version);
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
    }
}
