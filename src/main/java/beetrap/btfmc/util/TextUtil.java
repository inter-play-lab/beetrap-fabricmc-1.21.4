package beetrap.btfmc.util;

import java.util.ArrayList;
import java.util.List;

public final class TextUtil {

    private TextUtil() {
        throw new AssertionError();
    }

    /**
     * Wraps text to fit within a specified character limit per line.
     * Respects word boundaries and handles long words by splitting them.
     * When words are split, they can be reconstructed without spaces.
     * 
     * @param text the text to wrap
     * @param maxCharsPerLine the maximum number of characters per line
     * @return a list of wrapped text lines
     */
    public static List<String> wrapText(String text, int maxCharsPerLine) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // Check if adding this word would exceed the character limit
            if (currentLine.length() + word.length() + (currentLine.length() > 0 ? 1 : 0) > maxCharsPerLine) {
                // If current line is not empty, add it to lines and start a new line
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                // If the word itself is longer than maxCharsPerLine, split it
                if (word.length() > maxCharsPerLine) {
                    while (word.length() > maxCharsPerLine) {
                        lines.add(word.substring(0, maxCharsPerLine));
                        word = word.substring(maxCharsPerLine);
                    }
                    if (word.length() > 0) {
                        currentLine.append(word);
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                // Add word to current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        // Add the last line if it's not empty
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

}
