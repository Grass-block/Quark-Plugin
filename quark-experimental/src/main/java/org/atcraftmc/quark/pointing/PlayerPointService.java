package org.atcraftmc.quark.pointing;

import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.ServiceInject;

import java.util.HashMap;
import java.util.Map;

@QuarkService(id = "player-points")
public final class PlayerPointService {
    private static final Map<String, PlayerPointService> INSTANCES = new HashMap<>();
    private static final AbstractCommand COMMAND = new PointManagementCommand();
    private final String id;

    public PlayerPointService(String id) {
        this.id = id;
    }

    public static PlayerPointService getInstance(String id) {
        return INSTANCES.computeIfAbsent(id, PlayerPointService::new);
    }

    private static long read(Player player, String id) {
        return PlayerDataService.get(player).getCompoundTag("points").getLong(id);
    }

    private static void set(Player player, String id, long value) {
        PlayerDataService.get(player).getCompoundTag("points").setLong(id, value);
        PlayerDataService.save(player);
    }

    public void set(Player player, long value) {
        set(player, this.id, value);
    }

    public long get(Player player) {
        return read(player, this.id);
    }

    public void add(Player player, int value) {
        set(player, this.id, get(player) + value);
    }

    public boolean cost(Player player, int amount) {
        var current = read(player, id);

        if (current < amount) {
            return false;
        }

        set(player, current - amount);

        return true;
    }

    public long move(Player from, Player to, int amount) {
        var current = get(from);

        var moved = current < amount ? current : current - amount;

        add(to, amount);

        return moved;
    }


    @ServiceInject
    public void start() {
        Quark.getInstance().getCommandManager().register(COMMAND);
    }

    @ServiceInject
    public void stop() {
        Quark.getInstance().getCommandManager().unregister(COMMAND);
    }

    @QuarkCommand(name = "points", playerOnly = true)
    static final class PointManagementCommand extends AbstractCommand {


        @Override
        public void execute(CommandExecution context) {
            var service = getInstance(context.requireEnum(1, INSTANCES.keySet()));
        }

        @Override
        public void suggest(CommandSuggestion suggestion) {

        }
    }

}
