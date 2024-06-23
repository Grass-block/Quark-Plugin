package org.tbstcraft.quark.internal.http;

public enum ContentType {
    TEXT("text/plain;encoding=utf-8"),
    JSON("application/json;encoding=utf-8"),
    HTML("text-html;encoding=utf-8");

    final String mimeType;

    ContentType(String s) {
        this.mimeType = s;
    }

    @Override
    public String toString() {
        return this.mimeType;
    }
}
