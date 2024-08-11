package org.tbstcraft.quark.foundation.command.assertion;

public final class CommandAssertionException extends RuntimeException {
    private final String code;
    private final Object[] info;

    public CommandAssertionException(String code, Object... info) {
        this.code = code;
        this.info = info;
    }

    public Object[] getInfo() {
        return info;
    }

    public String getCode() {
        return code;
    }
}
