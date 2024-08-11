package org.tbstcraft.quark.foundation.command.assertion;

public final class ArgumentAssertionException extends RuntimeException {
    private final int position;
    private final String code;
    private final Object[] info;

    public ArgumentAssertionException(String code, int position, Object... info) {
        this.position = position;
        this.code = code;
        this.info = info;
    }

    public Object[] getInfo() {
        return info;
    }

    public String getCode() {
        return code;
    }

    public int getPosition() {
        return position;
    }
}
