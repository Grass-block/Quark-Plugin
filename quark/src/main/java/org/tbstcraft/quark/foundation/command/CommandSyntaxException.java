package org.tbstcraft.quark.foundation.command;

public class CommandSyntaxException extends RuntimeException {
    private final AbstractCommand command;
    private final String[] args;
    private final CommandArg[] types;

    public CommandSyntaxException(AbstractCommand command, String[] args, CommandArg[] types) {
        this.command = command;
        this.args = args;
        this.types = types;
    }

    public CommandArg[] getTypes() {
        return types;
    }

    public String[] getArgs() {
        return args;
    }

    public AbstractCommand getCommand() {
        return command;
    }
}
