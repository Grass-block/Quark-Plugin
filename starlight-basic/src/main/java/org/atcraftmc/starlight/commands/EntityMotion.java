package org.atcraftmc.starlight.commands;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.bukkit.task.Task;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.command.select.EntitySelector;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

@ApplicationModule(id = "entity-motion")
@AutoRegister(Registrations.SERVER_EVENT)
@QuarkCommand(name = "motion", permission = "-starlight.command.motion")
public final class EntityMotion extends SLCommandModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(() -> Location.class.getDeclaredMethod("getNearbyEntities", double.class, double.class, double.class));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        EntitySelector.tab(suggestion, 0);
        suggestion.suggest(1, "add", "set");
        suggestion.suggest(2, "[time]", "1", "5", "10");
        suggestion.suggest(3, "[x]", "0", "1.0", "-1.0");
        suggestion.suggest(4, "[y]", "0", "1.0", "-1.0");
        suggestion.suggest(5, "[z]", "0", "1.0", "-1.0");
    }

    @Override
    public void execute(CommandExecution context) {
        var target = EntitySelector.selectEntity(context, 0);
        var mode = context.requireEnum(1, "add", "set");
        var add = mode.equals("add");
        var time = context.requireArgumentInteger(2);

        var x = context.requireArgumentDouble(3);
        var y = context.requireArgumentDouble(4);
        var z = context.requireArgumentDouble(5);

        var vector = new Vector(x, y, z);

        for (var p : target) {
            attempt((Entity) p, time, vector, add);
        }

        if (!(context.getSender() instanceof Player)) {
            return;
        }
        MessageAccessor.send(this.handle().getLanguage(), context.getSender(), "hint", target.size(), time, mode, x, y, z);
    }

    private void attempt(Entity e, int time, Vector value, boolean add) {
        if (e.getVehicle() != null) {
            attempt(e.getVehicle(), time, value, add);
        }

        TaskService.entity(e).timer(1, 1, new Consumer<>() {
            private int tick = 0;

            @Override
            public void accept(Task task) {
                this.tick++;

                if (this.tick > time) {
                    task.cancel();
                }

                e.setVelocity(add ? e.getVelocity().add(value) : value);
            }
        });
    }
}
