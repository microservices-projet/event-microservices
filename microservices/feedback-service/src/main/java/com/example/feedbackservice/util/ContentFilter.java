package com.example.feedbackservice.util;

import java.util.List;
import java.util.regex.Pattern;

public final class ContentFilter {

    private static final List<String> BANNED_WORDS = List.of(
            "spam", "scam", "fake", "arnaque", "fraude",
            "merde", "putain", "connard", "salaud", "enculé",
            "shit", "fuck", "damn", "asshole", "bastard"
    );

    private static final Pattern BANNED_PATTERN;

    static {
        String joined = String.join("|", BANNED_WORDS.stream()
                .map(Pattern::quote)
                .toList());
        BANNED_PATTERN = Pattern.compile("(?i)\\b(" + joined + ")\\b");
    }

    private ContentFilter() {}

    public static boolean containsInappropriateContent(String text) {
        if (text == null || text.isBlank()) return false;
        return BANNED_PATTERN.matcher(text).find();
    }

    public static String getFlagReason(String text) {
        if (text == null || text.isBlank()) return null;
        var matcher = BANNED_PATTERN.matcher(text);
        if (matcher.find()) {
            return "Contenu inapproprié détecté : mot interdit trouvé";
        }
        return null;
    }
}
