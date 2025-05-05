package org.example.demo.services.posts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ProfanityFilterService {

    private Set<String> profanityList = new HashSet<>(Arrays.asList(
            "salope", "putain", "damn", "hell", "crap", "stupid", "idiot"
    ));

    public ProfanityFilterService() {
        try {
            loadProfanityListFromResource("/profanity_words.txt");
        } catch (IOException e) {
            System.err.println("Using default profanity list. Error loading from resource: " + e.getMessage());
        }
    }

    private void loadProfanityListFromResource(String resourcePath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                Set<String> words = new HashSet<>();
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    words.add(line.toLowerCase());
                }

                if (!words.isEmpty()) {
                    profanityList = words;
                }
            }
        }
    }

    public boolean containsProfanity(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lowerCaseText = text.toLowerCase();

        for (String word : profanityList) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(lowerCaseText).find()) {
                return true;
            }
        }

        return false;
    }

    public String censorText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String result = text;

        for (String word : profanityList) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            String replacement = "*".repeat(word.length());
            result = pattern.matcher(result).replaceAll(replacement);
        }

        return result;
    }

    public String getSuggestedText(String text) {
        return censorText(text);
    }

    public static boolean createDefaultProfanityResource(String filePath) {
        String defaultContent =
                "badword1\n" +
                        "salope\n" +
                        "putain\n" +
                        "damn\n" +
                        "hell\n" +
                        "crap\n" +
                        "stupid\n" +
                        "idiot\n";

        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of(filePath), defaultContent);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to create profanity resource file: " + e.getMessage());
            return false;
        }
    }
}
