package org.zaproxy.addon.exim.sites;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class YamlFormatter {
    private static final String NEWLINE = "\n";
    private static final String BASE_INDENT = "  ";
    private static final Set<String> KNOWN_KEYS = new HashSet<>(Arrays.asList(
            "node", "url", "method", "data", "responseLength", "children", "statusCode"
    ));

    public static String formatYamlString(String yamlStr) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = yamlStr.split("\n");
        StringBuilder currentValue = new StringBuilder();
        String currentKey = null;
        boolean isFirst = true;
        boolean isMultiLine = false;

        for (String s : lines) {
            String line = s.trim();

            // Skip empty lines
            if (line.isEmpty()) continue;

            // Check if line starts with a known key (accounting for "- " prefix)
            if (line.startsWith("- ")) {
                line = line.substring(2);
            }

            boolean isKnownKey = false;
            for (String key : KNOWN_KEYS) {
                if (line.startsWith(key + ":")) {
                    // If we have a previous key-value pair to write
                    if (currentKey != null) {
                        writeFormattedKeyValue(formatted, currentKey, currentValue.toString().trim(), isFirst, isMultiLine);
                        isFirst = false;
                        isMultiLine = false;
                    }

                    // Start new key-value pair
                    currentKey = key;
                    currentValue = new StringBuilder();
                    String value = line.substring(key.length() + 1).trim();
                    if (!value.isEmpty()) {
                        currentValue.append(value);
                    }
                    isKnownKey = true;
                    break;
                }
            }

            if (!isKnownKey && currentKey != null) {
                // This is a continuation line
                if (!currentValue.isEmpty()) {
                    currentValue.append("  "); // Preserve two spaces between lines
                }
                currentValue.append(line);
                isMultiLine = true;
            }
        }

        // Write the last key-value pair
        if (currentKey != null) {
            writeFormattedKeyValue(formatted, currentKey, currentValue.toString().trim(), isFirst, isMultiLine);
        }

        return formatted.toString();
    }

    private static void writeFormattedKeyValue(StringBuilder builder, String key, String value, boolean isFirst, boolean isMultiLine) {
        if (isFirst) {
            builder.append("- ");
        } else {
            builder.append(BASE_INDENT);
        }

        builder.append(key).append(": ");

//         Quote multi-line values or values containing special characters
        if (isMultiLine || value.contains(":") || value.contains("#") || value.contains("'")) {
            builder.append("\"").append(value.replace("\"", "\\\"")).append("\"");
        } else {
            builder.append(value);
        }

        builder.append(NEWLINE);
    }
}