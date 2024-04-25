package org.tbstcraft.quark.util;

public interface Identifiers {
    char INTERNAL_LINE = '_';
    char EXTERNAL_LINE = '-';
    char SEPARATOR = ':';

    static String internal(String id) {
        return id.replace(EXTERNAL_LINE, INTERNAL_LINE);
    }

    static String external(String id) {
        return id.replace(INTERNAL_LINE, EXTERNAL_LINE);
    }

    static String external(String ns, String id) {
        return external(ns + SEPARATOR + id);
    }

    static String internal(String ns, String id) {
        return internal(ns + SEPARATOR + id);
    }

    static String internalNS(String id){
        return internal(id.split(String.valueOf(SEPARATOR))[0]);
    }

    static String externalNS(String id){
        return external(id.split(String.valueOf(SEPARATOR))[0]);
    }

    static String internalId(String id){
        return internal(id.split(String.valueOf(SEPARATOR))[1]);
    }

    static String externalId(String id){
        return external(id.split(String.valueOf(SEPARATOR))[1]);
    }
}
