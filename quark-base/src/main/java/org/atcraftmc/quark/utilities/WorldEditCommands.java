package org.atcraftmc.quark.utilities;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.Bukkit;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@QuarkModule
@CommandProvider({WorldEditCommands.MirrorCommand.class, WorldEditCommands.DrainWaterCommand.class, WorldEditCommands.FastBrushCommand.class})
public final class WorldEditCommands extends PackageModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requirePlugin("WorldEdit");
    }

    @QuarkCommand(name = "/mirror")
    public static final class MirrorCommand extends ModuleCommand<WorldEditCommands> {
        @Override
        public void execute(CommandExecution context) {
            var player = context.requireSenderAsPlayer();

            Bukkit.dispatchCommand(player, "/copy");
            Bukkit.dispatchCommand(player, "/flip");
            Bukkit.dispatchCommand(player, "/paste");
        }
    }

    @QuarkCommand(name = "/drain-water")
    public static final class DrainWaterCommand extends ModuleCommand<WorldEditCommands> {
        @Override
        public void execute(CommandExecution context) {
            var player = context.requireSenderAsPlayer();

            Bukkit.dispatchCommand(player, "/set ^[waterlogged=false]");
            Bukkit.dispatchCommand(player, "/replace water air");
        }
    }

    @QuarkCommand(name = "/fast-brash")
    public static final class FastBrushCommand extends ModuleCommand<WorldEditCommands> {
        public static final String[] TREES = {"acacia", "birch", "brownmushroom", "cherry", "chorusplant", "crimsonfungus", "darkoak", "jungle", "junglebush", "largeoak", "largespruce", "mangrove", "oak", "pine", "rand", "randbirch", "randjungle", "randmushroom", "randspruce", "redmushroom", "randspruce",  // Duplicate
                "shortjungle", "smalljungle", "spruce", "swamp", "tall mangrove", "tallbirch", "tallspruce", "warpedfungus"};

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, TREES);
            suggestion.suggest(0, "grasses");
        }

        @Override
        public void execute(CommandExecution context) {
            var player = context.requireSenderAsPlayer();

            var command = switch (context.requireArgumentAt(0)) {
                case "acacia" -> "/br forest sphere 0.1 acacia";
                case "birch" -> "/br forest sphere 0.1 birch";
                case "brownmushroom" -> "/br forest sphere 0.1 brownmushroom";
                case "cherry" -> "/br forest sphere 0.1 cherry";
                case "chorusplant" -> "/br forest sphere 0.1 chorusplant";
                case "crimsonfungus" -> "/br forest sphere 0.1 crimsonfungus";
                case "darkoak" -> "/br forest sphere 0.1 darkoak";
                case "jungle" -> "/br forest sphere 0.1 jungle";
                case "junglebush" -> "/br forest sphere 0.1 junglebush";
                case "largeoak" -> "/br forest sphere 0.1 largeoak";
                case "largespruce" -> "/br forest sphere 0.1 largespruce";
                case "mangrove" -> "/br forest sphere 0.1 mangrove";
                case "oak" -> "/br forest sphere 0.1 oak";
                case "pine" -> "/br forest sphere 0.1 pine";
                case "rand" -> "/br forest sphere 0.1 rand";
                case "randbirch" -> "/br forest sphere 0.1 randbirch";
                case "randjungle" -> "/br forest sphere 0.1 randjungle";
                case "randmushroom" -> "/br forest sphere 0.1 randmushroom";
                case "randspruce" -> "/br forest sphere 0.1 randspruce";
                case "redmushroom" -> "/br forest sphere 0.1 redmushroom";
                case "shortjungle" -> "/br forest sphere 0.1 shortjungle";
                case "smalljungle" -> "/br forest sphere 0.1 smalljungle";
                case "spruce" -> "/br forest sphere 0.1 spruce";
                case "swamp" -> "/br forest sphere 0.1 swamp";
                case "tall_mangrove" -> "/br forest sphere 0.1 tall mangrove";
                case "tallbirch" -> "/br forest sphere 0.1 tallbirch";
                case "tallspruce" -> "/br forest sphere 0.1 tallspruce";
                case "warpedfungus" -> "/br forest sphere 0.1 warpedfungus";
                case "grasses" -> "/br apply sphere 0.1 item bone_meal";
                default -> throw new IllegalStateException("Unexpected value: " + context.requireArgumentAt(0));
            };

            Bukkit.dispatchCommand(player, command);
        }
    }
}
