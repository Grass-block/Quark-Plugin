package org.atcraftmc.starlight.management;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.bukkit.Bukkit;
import org.bukkit.World;

@QuarkCommand(name = "system", permission = "-quark.management.system")
@SLModule
public final class ServerInfo extends CommandModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(() -> World.class.getMethod("getChunkCount"));
    }

    @Override
    public void execute(CommandExecution context) {
        msg(context, """
                {#line}
                {#purple}Server info:
                {#gray}- {#dark_aqua}Players: {#aqua} %s
                {#gray}- {#dark_aqua}TPS: {#tps} {#dark_green}MSPT: {#green}{#mspt}
                {#gray}- {#dark_aqua}Memory: {#aqua} %sMB / %sMB
                """.formatted(
                Bukkit.getOnlinePlayers().size(),
                Runtime.getRuntime().totalMemory() / 1049576,
                Runtime.getRuntime().maxMemory() / 1049576
        ));

        msg(context, "{#gray}- {#dark-aqua}Worlds:");
        for (var i = 0; i < Bukkit.getWorlds().size(); i++) {
            var world = Bukkit.getWorlds().get(i);
            msg(context, world(world, i));
        }

        msg(context, "{#line}");
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {

    }

    private void msg(CommandExecution ctx, String msg) {
        TextSender.sendMessage(ctx.getSender(), TextBuilder.buildComponent(msg));
    }

    private String world(World world, int id) {
        var entities = world.getEntityCount();
        var te = world.getTileEntityCount();
        var te_t = world.getTickableTileEntityCount();
        var players = world.getPlayerCount();
        var c = "{#dark_aqua}Chunks: {#aqua}%s".formatted(world.getChunkCount());
        var e = "{#dark_green}Entities: {#green}%s{#gray}(Players:{#green}%s{#gray})".formatted(entities, players);
        var be = "{#dark_purple}TileEntities: {#purple}%s{#gray}(Tick: {#purple}%s{#gray})".formatted(te, te_t);

        var wid = "{hover(text,%s{#gray}(%s))}[%s%s{#white}]".formatted(world.getName(), world.getUID(), switch (world.getEnvironment()) {
            case NORMAL -> "{#green}";
            case NETHER -> "{#red}";
            case THE_END -> "{#purple}";
            default -> "{#aqua}";
        }, id);


        return "  %s %s %s %s{;}".formatted(wid, c, e, be);
    }

}
