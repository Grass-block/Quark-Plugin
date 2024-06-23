package org.tbstcraft.quark.foundation.command.driver;

import org.bukkit.command.CommandSender;

import java.util.Objects;

public class CommandExecutorInstance {
    private CommandExecutor executor;

    public void execute(CommandSender sender, String[] args) {

    }

    public int match(String command, String[] args) {
        String[] path = this.executor.path().split(" ");

        if (!Objects.equals(command, args[0])) {
            return -1;
        }

        if (path.length != args.length + 1) {
            return -1;
        }

        for (int i = 0; i < args.length; i++) {
            String template = path[i + 1];
            if(template.matches("[\\[{<]+")) {
                continue;
            }
            if(!template.equals(args[i])){
                return -1;
            }
        }

        return 0;
    }
}
