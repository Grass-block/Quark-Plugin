package org.tbstcraft.quark.display;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.command.execute.CommandExecution;
import org.tbstcraft.quark.foundation.command.execute.CommandExecutor;
import org.tbstcraft.quark.foundation.command.execute.CommandSuggestion;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@QuarkModule(version = "1.0-beta", beta = true)
@CommandProvider(Nickname.NickNameCommand.class)
public final class Nickname extends PackageModule implements CommandExecutor {
    private final ProtocolManager service = ProtocolLibrary.getProtocolManager();

    private final PacketListener playerData = new PacketAdapter(Quark.getInstance(), PacketType.Play.Server.PLAYER_INFO) {
        @Override
        public void onPacketSending(PacketEvent event) {
            var packet = event.getPacket();
            var infos = packet.getPlayerInfoDataLists().read(0);

            for (PlayerInfoData data : infos) {
                if (data == null) {
                    continue;
                }

                var player = Bukkit.getPlayer(data.getProfile().getName());
                if (player == null) {
                    continue;
                }

                NBTTagCompound entry = PlayerDataService.getEntry(player.getName(), getFullId());

                if (!entry.hasKey("value")) {
                    continue;
                }

                var profile = new WrappedGameProfile(data.getProfile().getUUID(), entry.getString("value"));
                var display = data.getDisplayName().deepClone();

                display.setJson("{\"text\":\"%s\"}".formatted(entry.getString("value")));

                PlayerInfoData newData = new PlayerInfoData(profile, data.getLatency(), data.getGameMode(), display);
                infos.set(infos.indexOf(data), newData);
            }
            packet.getPlayerInfoDataLists().write(0, infos);
        }
    };

    @Inject("-quark.nickname.other")
    public Permission setOtherPermission;

    @Override
    public void enable() {
        this.service.addPacketListener(this.playerData);
    }

    @Override
    public void disable() {
        this.service.removePacketListener(this.playerData);
    }

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("com.comphenix.protocol.ProtocolLibrary"));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "<name>");
    }

    @Override
    public void execute(CommandExecution context) {
        var name = context.requireArgumentAt(0);
        var player = context.hasArgumentAt(1) ? context.requirePlayer(1) : context.requireSenderAsPlayer();
        var isSelf = player == context.requireSenderAsPlayer();

        if (!isSelf) {
            context.requirePermission(this.setOtherPermission);
        }

        NBTTagCompound entry = PlayerDataService.getEntry(player.getName(), getFullId());
        entry.setString("value", name);
        PlayerDataService.save(player.getName());

        getLanguage().sendMessage(context.getSender(), isSelf ? "set-self" : "set-other", player.getName(), name);
    }

    @QuarkCommand(name = "nickname", permission = "+quark.nickname")
    public static final class NickNameCommand extends ModuleCommand<Nickname> {
        @Override
        public void init(Nickname module) {
            setExecutor(module);
        }
    }
}
