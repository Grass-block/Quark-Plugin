package org.tbstcraft.quark.util;

import java.util.logging.Logger;

public interface ExceptionUtil {
    static String getMessage(Throwable throwable) {
        return throwable.getClass().getName() + " -> " + throwable.getMessage();
    }

    static String getStackTraceMessage(StringBuilder sb, Throwable throwable) {
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("  ").append(element.toString()).append("\n");
        }
        Throwable cause = throwable.getCause();
        if (cause != null) {
            sb.append("Caused by: ").append(cause.getMessage()).append("\n").append(getStackTraceMessage(sb, cause));
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    static String getFullMessage(Throwable throwable) {
        return getMessage(throwable) + "\n" + getStackTraceMessage(new StringBuilder(), throwable);
    }

    static void log(Logger logger, Throwable throwable) {
        for (String s : getFullMessage(throwable).split("\n")) {
            logger.severe(s);
        }
    }

    static void log(Throwable throwable) {
        throwable.printStackTrace(System.out);
    }
}
