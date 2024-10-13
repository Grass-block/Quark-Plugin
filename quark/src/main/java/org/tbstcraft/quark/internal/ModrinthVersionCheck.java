package org.tbstcraft.quark.internal;

import me.gb2022.commons.TriState;
import me.gb2022.commons.http.HTTPUtil;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommandExecutor;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.function.BiConsumer;

@QuarkModule(internal = true)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ModrinthVersionCheck extends PackageModule implements QuarkCommandExecutor {
    public static final String API = "https://api.modrinth.com/v2/project/quark-plugin/version";
    public static final String VERSION_PAGE = "https://modrinth.com/plugin/quark-plugin/version/%s";

    @Inject("-quark.version.announce")
    private Permission updateAnnounce;
    
    @Inject
    private LanguageEntry language;

    @Inject
    private Logger logger;

    private TriState cachedState;
    private String cachedVersion;

    @Override
    public void enable() {
        getHandle().getCommand("quark").registerSubCommand(new CheckVersionCommand(this));

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
            this.language.sendMessage(event.getPlayer(), "require", cachedVersion, page);
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.language.sendMessage(sender, "checking");
        this.check((state, version) -> {
            switch (state) {
                case TRUE -> {
                    String page = VERSION_PAGE.formatted(this.cachedVersion);
                    language.sendMessage(sender, "require", version, page);
                }
                case FALSE -> language.sendMessage(sender, "no-require", version);
                case UNKNOWN -> language.sendMessage(sender, "exception");
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
