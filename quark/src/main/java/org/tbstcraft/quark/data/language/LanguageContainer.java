package org.tbstcraft.quark.data.language;

import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.util.Identifiers;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LanguageContainer {
    public static final LanguageContainer INSTANCE = new LanguageContainer();

    private final Map<String, Map<String, Object>> items = new HashMap<>();
    private final Map<String, LanguagePack> packs = new HashMap<>();


    public static LanguageContainer getInstance() {
        return INSTANCE;
    }

    public static String key(String pack, String entry, String id) {
        return "%s:%s:%s".formatted(Identifiers.external(pack), Identifiers.external(entry), Identifiers.external(id));
    }

    static String list2string(List<String> list) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String ss : list) {
            i++;
            sb.append(ss);
            if (i < list.size()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public Map<String, Object> getItemEntry(String key) {
        if (!key.matches("[a-z0-9-]+:[a-z0-9-]+:[a-z0-9-]+")) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }

        if (!this.items.containsKey(key)) {
            this.items.put(key, new HashMap<>());
        }

        return this.items.get(key);
    }

    public Map<String, Map<String, Object>> getItems() {
        return items;
    }

    //----[Packs]----
    public void inject(LanguagePack pack) {
        for (String k : pack.getKeys()) {
            String key = pack.getId() + ":" + k;

            getItemEntry(key).put(pack.getLocale(), pack.getObject(k));
        }
    }

    public void refresh(boolean clean) {
        if (clean) {
            this.items.clear();
        }
        for (LanguagePack pack : this.packs.values()) {
            this.inject(pack);
        }
    }

    public void register(LanguagePack pack) {
        this.packs.put(pack.toString(), pack);
        this.inject(pack);
    }

    public void unregister(LanguagePack pack) {
        this.packs.remove(pack.toString());
    }

    public Collection<LanguagePack> getPacks() {
        return packs.values();
    }

    public LanguagePack getPack(String id) {
        return this.packs.get(id);
    }

    public Map<String, LanguagePack> getPackStorage() {
        return this.packs;
    }

    //----[Access]----
    public ILanguageAccess access(String pack) {
        return new LanguageAccess(this, pack);
    }

    public LanguageEntry entry(String pack, String entry) {
        return new LanguageEntry(access(pack), entry);
    }

    public LanguageItem item(String pack, String entry, String id) {
        return new LanguageItem(access(pack), entry, id);
    }

    //----[raw]----
    public String error(Locale locale, String pack, String entry, String id) {
        return key(pack, entry, id) + "@" + LocaleMapping.minecraft(locale);
    }

    public Object getObject(Locale locale, String pack, String entry, String id) {
        Map<String, Object> item = getItemEntry(key(pack, entry, id));
        String loc = LocaleMapping.remap(LocaleMapping.minecraft(locale), item::containsKey);

        return item.get(loc);
    }

    public String getRawMessage(Locale locale, String pack, String entry, String id) {
        Object obj = getObject(locale, pack, entry, id);
        if (obj == null) {
            return error(locale, pack, entry, id);
        }

        if (obj instanceof String s) {
            return s;
        }

        @SuppressWarnings("unchecked") List<String> list = ((List<String>) obj);

        return list2string(list);
    }

    public List<String> getRawMessageList(Locale locale, String pack, String entry, String id) {
        var obj = getObject(locale, pack, entry, id);
        if (obj == null) {
            return Collections.singletonList(error(locale, pack, entry, id));
        }

        if (!(obj instanceof @SuppressWarnings("rawtypes")List list)) {
            return Collections.singletonList(error(locale, pack, entry, id));
        }

        if (list.get(0) instanceof String) {
            @SuppressWarnings("unchecked") var l = ((List<String>) obj);
            return l;
        }

        @SuppressWarnings("unchecked") var l = ((List<List<String>>) list);
        List<String> lst = new ArrayList<>(list.size());

        for (List<String> item : l) {
            lst.add(list2string(item));
        }
        return lst;
    }

    public String getRawRandomMessage(Locale locale, String pack, String entry, String id) {
        List<String> items = getRawMessageList(locale, pack, entry, id);
        return items.get(SharedObjects.RANDOM.nextInt(0, items.size()));
    }


    //----[inline]----
    public String getInlineMessage(Locale locale, String pack, String entry, String id) {
        String msg = getRawMessage(locale, pack, entry, id);
        return inline(msg, locale, pack, entry);
    }

    public String getInlineRandomMessage(Locale locale, String pack, String entry, String id) {
        String msg = getRawRandomMessage(locale, pack, entry, id);
        return inline(msg, locale, pack, entry);
    }

    public List<String> getInlineMessageList(Locale locale, String pack, String entry, String id) {
        List<String> msg = getRawMessageList(locale, pack, entry, id);

        msg.replaceAll(src -> inline(src, locale, pack, entry));
        return msg;
    }

    private String inlineKey(String k, String pack, String entry) {
        return switch (k.split(":").length) {
            case 1 -> pack + ":" + entry + ":" + k;
            case 2 -> pack + ":" + k;
            default -> k;
        };
    }

    private String match(String src, Pattern pattern, BiFunction<String, String, String> process) {
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            src = process.apply(src, matcher.group());
        }
        return src;
    }

    public String inline(String source, Locale locale, String pack, String entry) {
        source = match(source, Language.MESSAGE_PATTERN, (src, s) -> {
            String[] key = inlineKey(s.substring(5, s.length() - 1), pack, entry).split(":");
            return src.replace(s, getInlineMessage(locale, key[0], key[1], key[2]));
        });

        source = match(source, Language.RANDOM_MESSAGE_PATTERN, (src, s) -> {
            String[] key = inlineKey(s.substring(6, s.length() - 1), pack, entry).split(":");
            return src.replace(s, getInlineRandomMessage(locale, key[0], key[1], key[2]));
        });

        source = match(source, Language.LOCALIZED_GLOBAL_VAR, (src, s) -> {
            String id = s.substring(8, s.length() - 1);
            String inline = getInlineMessage(locale, "quark-core", "global-vars", id);
            return src.replace(s, inline);
        });

        return PlaceHolderService.format(PlaceHolderService.format(source));
    }


    //----[complete]----
    public String getMessage(Locale locale, String pack, String entry, String id, Object... format) {
        return Language.format(getInlineMessage(locale, pack, entry, id), format);
    }

    public String getRandomMessage(Locale locale, String pack, String entry, String id, Object... format) {
        return Language.format(getInlineRandomMessage(locale, pack, entry, id), format);
    }

    public List<String> getMessageList(Locale locale, String pack, String entry, String id) {
        return getInlineMessageList(locale, pack, entry, id);
    }


    //----[check]----
    public boolean hasAny(String pack, String entry, String id) {
        return !getItemEntry(key(pack, entry, id)).isEmpty();
    }

    public boolean has(Locale locale, String pack, String entry, String id) {
        if (!this.hasAny(pack, entry, id)) {
            return false;
        }
        return getItemEntry(key(pack, entry, id)).containsKey(LocaleMapping.minecraft(locale));
    }
}
