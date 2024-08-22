package org.tbstcraft.quark.deprecated.command_driver;

public interface CommandIdentifiers {

    static String commandName(String raw) {
        return raw.split(" ")[0].replaceFirst("/", "");
    }

    static String make(String command, String[] args) {
        StringBuilder sb = new StringBuilder();

        sb.append(command);
        for (String arg : args) {
            sb.append(" ");
            sb.append(arg);
        }
        return sb.toString();
    }
}
