package org.example.demo.utils;

import java.util.List;
import java.util.regex.Pattern;

public class TextUtils {
    /**
     * Remplace chaque mot de la liste badWords par autant d'astérisques que la longueur du mot,
     * en respectant les frontières de mots et en ignorant la casse.
     *
     * Ex : "test réclamation" → "**** réclamation"
     */
    public static String maskBadWords(String input, List<String> badWords) {
        String result = input;
        for (String bad : badWords) {
            // \b pour frontière de mot, (?i) pour insensible à la casse
            String regex = "(?i)\\b" + Pattern.quote(bad) + "\\b";
            String replacement = "*".repeat(bad.length());
            result = result.replaceAll(regex, replacement);
        }
        return result;
    }
}
