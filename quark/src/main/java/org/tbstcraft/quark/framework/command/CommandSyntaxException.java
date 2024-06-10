package org.tbstcraft.quark.framework.command;

public class CommandSyntaxException extends RuntimeException {
    private final AbstractCommand command;
    private final String[] args;
    private final CommandArgumentType[] types;

    public CommandSyntaxException(AbstractCommand command, String[] args, CommandArgumentType[] types) {
        this.command = command;
        this.args = args;
        this.types = types;
    }

    public CommandArgumentType[] getTypes() {
        return types;
    }

    public String[] getArgs() {
        return args;
    }

    public AbstractCommand getCommand() {
        return command;
    }
}
