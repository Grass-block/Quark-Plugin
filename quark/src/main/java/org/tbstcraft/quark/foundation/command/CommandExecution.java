package org.tbstcraft.quark.foundation.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.foundation.command.assertion.ArgumentAssertionException;
import org.tbstcraft.quark.foundation.command.assertion.CommandAssertionException;
import org.tbstcraft.quark.foundation.command.assertion.NumberLimitation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
public final class CommandExecution {
    private final String[] args;
    private final CommandSender sender;

    public CommandExecution(CommandSender sender, String[] args) {
        this.sender = sender;
        this.args = args;
    }

    public String requireArgumentAt(int position) {
        if (this.args.length <= position) {
            throw new ArgumentAssertionException("missing", position);
        }
        return this.args[position];
    }

    public boolean hasArgumentAt(int position) {
        return this.args.length > position;
    }

    private void testNumber(double value, int position, NumberLimitation... requirements) {
        for (NumberLimitation lim : requirements) {
            lim.test(value, position);
        }
    }

    public int requireArgumentInteger(int position, NumberLimitation... requirements) {
        String arg = this.requireArgumentAt(position);
        try {
            int n = Integer.parseInt(arg);
            testNumber(n, position, requirements);
            return n;

        } catch (NumberFormatException e) {
            throw new ArgumentAssertionException("argument-type-int", position, arg);
        }
    }

    public float requireArgumentFloat(int position, NumberLimitation... requirements) {
        String arg = this.requireArgumentAt(position);
        try {
            float n = Float.parseFloat(arg);
            testNumber(n, position, requirements);
            return n;
        } catch (NumberFormatException e) {
            throw new ArgumentAssertionException("argument-type-float", position, arg);
        }
    }

    public double requireArgumentDouble(int position, NumberLimitation... requirements) {
        String arg = this.requireArgumentAt(position);
        try {
            double n = Double.parseDouble(arg);
            testNumber(n, position, requirements);
            return n;
        } catch (NumberFormatException e) {
            throw new ArgumentAssertionException("argument-type-double", position, arg);
        }
    }

    public String requireEnum(int position, String... accept) {
        String arg = requireArgumentAt(position);

        if (!List.of(accept).contains(arg)) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (String s : accept) {
                sb.append(s).append("|");
            }
            sb.append("]");

            sb.deleteCharAt(sb.length() - 2);

            throw new ArgumentAssertionException("argument-type-enum", position, sb.toString(), arg);
        }

        return arg;
    }

    public String requireEnum(int position, Set<String> accept) {
        return requireEnum(position, accept.toArray(new String[0]));
    }

    public Player requirePlayer(int position) {
        String name = this.requireArgumentAt(position);

        Player p = Bukkit.getPlayerExact(name);

        if (p == null) {
            throw new ArgumentAssertionException("require-player", position, name);
        }

        return p;
    }

    public OfflinePlayer requireOfflinePlayer(int position) {
        String name = this.requireArgumentAt(position);

        return Bukkit.getOfflinePlayer(name);
    }

    public CommandSender getSender() {
        return sender;
    }

    public String[] getArgs() {
        return args;
    }

    public int requireIntegerOrElse(int position, int fallback, NumberLimitation... bounds) {
        String input = this.requireArgumentAt(position);
        try {
            int n = Integer.parseInt(input);
            testNumber(n, position, bounds);
            return n;
        } catch (NumberFormatException e) {
            try {
                double val = Double.parseDouble(input);
                throw new ArgumentAssertionException("argument-type-int", position, val);
            } catch (ArgumentAssertionException ee) {
                throw ee;
            } catch (Exception ignored) {
            }

            return fallback;
        }
    }

    public void requirePermission(Permission permission) {
        if (this.sender.hasPermission(permission)) {
            return;
        }

        throw new CommandAssertionException("lack-permission", permission.getName());
    }

    public void requireAnyPermission(Permission... permissions) {
        for (Permission p : permissions) {
            if (this.sender.hasPermission(p)) {
                return;
            }
        }
        String[] names = new String[permissions.length];

        for (int i = 0; i < names.length; i++) {
            names[i] = permissions[i].getName();
        }

        throw new CommandAssertionException("lack-any-permission", Arrays.toString(names));
    }

    public void requireAllPermission(Permission... permissions) {
        for (Permission p : permissions) {
            if (!this.sender.hasPermission(p)) {
                String[] names = new String[permissions.length];

                for (int i = 0; i < names.length; i++) {
                    names[i] = permissions[i].getName();
                }

                throw new CommandAssertionException("lack-all-permission", Arrays.toString(names));
            }
        }
    }

    public Player requireSenderAsPlayer() {
        if (!(this.sender instanceof Player p)) {
            throw new CommandAssertionException("sender-is-not-player");
        }
        return p;
    }

    public void matchArgument(int position, String id, Runnable command) {
        if (Objects.equals(requireArgumentAt(position), id)) {
            command.run();
        }
    }
}
