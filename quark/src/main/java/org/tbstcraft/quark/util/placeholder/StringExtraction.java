package org.tbstcraft.quark.util.placeholder;

import java.util.regex.Pattern;

@SuppressWarnings("ClassCanBeRecord")
public final class StringExtraction {
    private final Pattern pattern;
    private final int preLength;
    private final int postLength;

    public StringExtraction(Pattern pattern, int preLength, int postLength) {
        this.pattern = pattern;
        this.preLength = preLength;
        this.postLength = postLength;
    }

    public static StringExtraction expression(String regExp, int preLength, int postLength) {
        return new StringExtraction(Pattern.compile(regExp), preLength, postLength);
    }

    public String extract(String key) {
        return key.substring(preLength, key.length() - postLength);
    }

    public int getPostLength() {
        return postLength;
    }

    public int getPreLength() {
        return preLength;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
