package io.github.marcus8448.chat.client.parse;

import javafx.scene.text.*;

import java.util.ArrayList;
import java.util.List;

public class MarkdownParser { // oh, no.
    /**
     * ONLY SUPPORTS BOLD, ITALIC, STRIKETHROUGH
     * @param text
     * @return
     */
    public static void parseMarkdown(TextFlow flow, String text) {
        flow.getChildren().clear();
        char[] arr = text.toCharArray();
        List<Integer> bold = new ArrayList<>();
        List<Integer> italics = new ArrayList<>();
        List<Integer> strikethrough = new ArrayList<>();
        for (int i = 0, len = arr.length; i < len; i++) {
            char c = arr[i];
            boolean hasnext = i + 1 < arr.length;
            if (c == '\\') i++; // skip this char and the next one
            if (c == '*') {
                if (hasnext && arr[i + 1] == '*') {
                    bold.add(i);
                    i++;
                } else {
                    italics.add(i);
                }
            } else if (c == '~') {
                if (hasnext && arr[i + 1] == '~') {
                    strikethrough.add(i);
                }
            }
        }
        if (bold.size() % 2 == 1) {
            bold.remove(bold.size() - 1);
        }
        if (italics.size() % 2 == 1) {
            italics.remove(italics.size() - 1);
        }
        if (strikethrough.size() % 2 == 1) {
            strikethrough.remove(strikethrough.size() - 1);
        }

        if ((bold.size() * 2 + italics.size() + strikethrough.size() * 2) == text.length() || (bold.isEmpty() && italics.isEmpty() && strikethrough.isEmpty())) {
            flow.getChildren().add(new Text(text));
            return;
        }

        boolean isBold = false;
        boolean isItalicized = false;
        boolean isStrikethrough = false;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (bold.contains(i) || strikethrough.contains(i) || italics.contains(i)) {
                append(flow, isBold, isItalicized, isStrikethrough, builder);
                builder.setLength(0);
                if (bold.contains(i)) {
                    isBold = !isBold;
                    i++; // skip second star
                } else if (strikethrough.contains(i)) {
                    isStrikethrough = !isStrikethrough;
                    i++; // skip second tilde
                } else if (italics.contains(i)) {
                    isItalicized = !isItalicized;
                }
            } else {
                builder.append(arr[i]);
            }
        }
        if (!builder.isEmpty()) {
            append(flow, isBold, isItalicized, isStrikethrough, builder);
        }
    }

    private static void append(TextFlow flow, boolean isBold, boolean isItalicized, boolean isStrikethrough, StringBuilder builder) {
        Text txt = new Text(builder.toString());
        txt.strikethroughProperty().set(isStrikethrough);
        txt.setFont(Font.font(Font.getDefault().getFamily(), isBold ? FontWeight.BOLD : FontWeight.NORMAL, isItalicized ? FontPosture.ITALIC : FontPosture.REGULAR, Font.getDefault().getSize()));
        flow.getChildren().add(txt);
    }
}
