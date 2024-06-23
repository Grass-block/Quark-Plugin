package org.tbstcraft.quark.data.language;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class LanguageAccess extends ILanguageAccess {
    private static final Random RANDOM = new Random();
    private final LanguageContainer parent;

    public LanguageAccess(LanguageContainer parent) {
        this.parent = parent;
    }


    public String getRawMessage(Locale locale, String namespace, String id) {
        return this.parent.getMessage(locale, namespace, id);
    }

    public List<String> getRawMessageList(Locale locale, String namespace, String id) {
        return this.parent.getMessageList(locale, namespace, id);
    }

    @Override
    public boolean hasKey(String namespace, String id) {
        return false;//todo
    }

    @Override
    public boolean hasNamespace(String namespace) {
        return false;//todo
    }
}
