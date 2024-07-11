package org.tbstcraft.quark.util.placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface PlaceHolder {
    static List<String> extract(Pattern pattern, String input) {
        List<String> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    static String handle(StringExtraction extraction, String input, Function<String, String> supporter) {
        List<String> keys = extract(extraction.getPattern(), input);
        for (String find : keys) {
            String extracted = extraction.extract(find);
            String replacement = supporter.apply(extracted);
            if (replacement == null) {
                continue;
            }
            input = input.replace(find, replacement);
        }
        return input;
    }

    static String format(StringExtraction extraction, String input, GloballyPlaceHolder... placeHolders) {
        return handle(extraction, input, (key) -> {
            for (GloballyPlaceHolder placeHolder : placeHolders) {
                if (placeHolder.has(key)) {
                    return placeHolder.get(key);
                }
            }
            return null;
        });
    }

    static <I> String formatObjective(StringExtraction extraction, I target, String input, ObjectivePlaceHolder<I>... placeHolders) {
        return handle(extraction, input, (key) -> {
            for (ObjectivePlaceHolder<I> placeHolder : placeHolders) {
                if (placeHolder.has(key)) {
                    return placeHolder.get(key, target);
                }
            }
            return null;
        });
    }
}
