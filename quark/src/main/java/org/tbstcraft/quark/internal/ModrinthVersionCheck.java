package org.tbstcraft.quark.internal;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.util.TriState;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.api.DelayedPlayerJoinEvent;
import org.tbstcraft.quark.foundation.command.CommandExecutor;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.ExceptionUtil;
import org.tbstcraft.quark.util.NetworkUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.function.BiConsumer;

@QuarkModule(version = "1.0", internal = true)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ModrinthVersionCheck extends PackageModule implements CommandExecutor {
    public static final String API = "https://api.modrinth.com/v2/project/quark-plugin/version";
    public static final String VERSION_PAGE = "https://modrinth.com/plugin/quark-plugin/version/%s";

    @Inject("-quark.version.announce")
    private Permission updateAnnounce;

    private TriState cachedState;
    private String cachedVersion;

    @Override
    public void enable() {
        CommandManager.getQuarkCommand("quark").registerSubCommand(new CheckVersionCommand(this));

        check((state, version) -> {
        });
    }

    private int calculateVersion(String version) {
        String[] subVersions = version.split("\\.");

        int num = 0;

        for (int i = subVersions.length - 1; i >= 0; i--) {
            num += (int) (Integer.parseInt(subVersions[i]) * Math.pow(1000, i));
        }

        return num;
    }

    public void check(BiConsumer<TriState, String> callback) {
        TaskService.asyncTask(() -> {
            try {
                HttpURLConnection con = NetworkUtil.getHttpURLConnection(API, false);
                var arr = SharedObjects.JSON_PARSER.parse(new String(con.getInputStream().readAllBytes())).getAsJsonArray();
                con.disconnect();

                var latest = arr.get(0).getAsJsonObject();

                var latestVersion = latest.get("version_number").getAsString();
                var currentVersion = Quark.PLUGIN.getDescription().getVersion();

                this.cachedVersion = latestVersion;

                if (calculateVersion(latestVersion) > calculateVersion(currentVersion)) {
                    callback.accept(TriState.TRUE, latestVersion);
                    this.cachedState = TriState.TRUE;
                    return;
                }
                callback.accept(TriState.FALSE, currentVersion);
                this.cachedState = TriState.FALSE;
            } catch (IOException e) {
                callback.accept(TriState.NOT_SET, null);
                ExceptionUtil.log(getLogger(), e);
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(DelayedPlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(this.updateAnnounce)) {
            return;
        }
        if (this.cachedState == TriState.TRUE) {
            String page = VERSION_PAGE.formatted(this.cachedVersion);
            getLanguage().sendMessage(event.getPlayer(), "require", page);
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.getLanguage().sendMessage(sender, "checking");
        this.check((state, version) -> {
            switch (state) {
                case TRUE -> {
                    String page = VERSION_PAGE.formatted(this.cachedVersion);
                    getLanguage().sendMessage(sender, "require", version, page);
                }
                case FALSE -> getLanguage().sendMessage(sender, "no-require", version);
                case NOT_SET -> getLanguage().sendMessage(sender, "exception");
            }
        });
    }

    @QuarkCommand(name = "check-version", permission = "-quark.version.check")
    public static final class CheckVersionCommand extends ModuleCommand<ModrinthVersionCheck> {
        public CheckVersionCommand(ModrinthVersionCheck module) {
            setExecutor(module);
        }
    }
}
