package org.atcraftmc.starlight.internal;

import me.gb2022.commons.TriState;
import me.gb2022.commons.http.HTTPUtil;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.function.BiConsumer;

@SLModule(internal = true)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ModrinthVersionCheck extends PackageModule implements PluginCommandExecutor {
    public static final String API = "https://api.modrinth.com/v2/project/quark-plugin/version";
    public static final String VERSION_PAGE = "https://modrinth.com/plugin/quark-plugin/version/%s";

    @Inject("-starlight.version.announce")
    private Permission updateAnnounce;

    @Inject
    private LanguageEntry language;

    @Inject
    private Logger logger;

    private TriState cachedState;
    private String cachedVersion;

    @Override
    public void enable() {
        getHandle().getCommand("starlight").registerSubCommand(new CheckVersionCommand(this));

        check((state, version) -> {
        });
    }

    private int calculateVersion(String version) {
        return Integer.parseInt(version.replaceAll("\\.", ""), 10);
    }

    public void check(BiConsumer<TriState, String> callback) {
        TaskService.async().run(() -> {
            try {
                HttpURLConnection con = HTTPUtil.getHttpURLConnection(API, false);
                var arr = SharedObjects.JSON_PARSER.parse(new String(con.getInputStream().readAllBytes())).getAsJsonArray();
                con.disconnect();

                var latest = arr.get(0).getAsJsonObject();

                var latestVersion = latest.get("version_number").getAsString();
                var currentVersion = getOwnerPlugin().getDescription().getVersion();

                this.cachedVersion = latestVersion;

                if (calculateVersion(latestVersion) > calculateVersion(currentVersion)) {
                    callback.accept(TriState.TRUE, latestVersion);
                    this.cachedState = TriState.TRUE;
                    return;
                }
                callback.accept(TriState.FALSE, currentVersion);
                this.cachedState = TriState.FALSE;
            } catch (IOException e) {
                callback.accept(TriState.UNKNOWN, null);
                this.logger.error("failed to check version", e);
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(this.updateAnnounce)) {
            return;
        }
        if (this.cachedState == TriState.TRUE) {
            String page = VERSION_PAGE.formatted(this.cachedVersion);
            this.language.item("require").send(event.getPlayer(), this.cachedVersion, page);
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.language.item("checking").send(sender);
        this.check((state, version) -> {
            switch (state) {
                case TRUE -> {
                    String page = VERSION_PAGE.formatted(this.cachedVersion);
                    language.item("require").send(sender, version, page);
                }
                case FALSE -> language.item("no-require").send(sender, version);
                case UNKNOWN -> language.item("exception").send(sender);
            }
        });
    }

    @QuarkCommand(name = "check-version", permission = "-starlight.version.check")
    public static final class CheckVersionCommand extends ModuleCommand<ModrinthVersionCheck> {
        public CheckVersionCommand(ModrinthVersionCheck module) {
            setExecutor(module);
        }
    }
}
