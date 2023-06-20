/*
 * Copyright 2023 marcus8448
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcus8448.chat.client.parse;

import javafx.scene.text.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Quick and dirty Markdown parser
 */
public class MarkdownParser {
    /**
     * Parses text as markdown and appends it to the given TextFlow
     *
     * @param text the text to parse
     * @implNote Only supports bold (**), italic (*), and strikethrough (~~)
     * @implNote If the text consists of only formatting chars, there's a chance that it will just remove all content
     */
    public static void parseMarkdown(TextFlow flow, String text) {
        flow.getChildren().clear();
        char[] arr = text.toCharArray();
        List<Integer> bold = new ArrayList<>(); // indices of bold format (**)
        List<Integer> italics = new ArrayList<>(); // indices of italics format (*)
        List<Integer> strikethrough = new ArrayList<>(); // indices of strikethrough format (~~)
        for (int i = 0, len = arr.length; i < len; i++) {
            char c = arr[i]; // the character at this position
            boolean hasNext = i + 1 < arr.length; // whether there is another character after this one
            if (c == '\\') i++; // skip this char and the next one
            if (c == '*') { // check if it is a star character
                if (hasNext && arr[i + 1] == '*') { // if there is a second star, it's bold
                    bold.add(i);
                    i++; // skip the second star
                } else { // no second star, so italics
                    italics.add(i);
                }
            } else if (c == '~') { // check if it's a tilde
                if (hasNext && arr[i + 1] == '~') { // two tildes = strikethrough
                    strikethrough.add(i);
                    i++; // skip the second tilde
                }
            }
        }
        if (bold.size() % 2 == 1) { // if there are mismatched bolds (must wrap text), just remove the last one
            bold.remove(bold.size() - 1);
        }
        if (italics.size() % 2 == 1) { // if there are mismatched italics (must wrap text), just remove the last one
            italics.remove(italics.size() - 1);
        }
        if (strikethrough.size() % 2 == 1) { // if there is mismatched strikethrough (must wrap text), just remove the last one
            strikethrough.remove(strikethrough.size() - 1);
        }

        // check if the formatting will just emit nothing
        if ((bold.size() * 2 + italics.size() + strikethrough.size() * 2) == text.length()
                || (bold.isEmpty() && italics.isEmpty() && strikethrough.isEmpty())) {
            flow.getChildren().add(new Text(text)); // just return the plaintext
            return;
        }

        boolean isBold = false; // whether text is currently bold
        boolean isItalicized = false; // whether text is currently italicized
        boolean isStrikethrough = false; // whether text has strikethrough

        StringBuilder builder = new StringBuilder(); // buffer for text
        for (int i = 0; i < arr.length; i++) { //iterate over all characters
            if (bold.contains(i) || strikethrough.contains(i) || italics.contains(i)) { // check if there is a style change
                if (!builder.isEmpty()) { // check if we have pending text
                    append(flow, isBold, isItalicized, isStrikethrough, builder); // append the text
                    builder.setLength(0); // clear the builder
                }
                if (bold.contains(i)) { // check if we are swapping bold state
                    isBold = !isBold;
                    i++; // skip second star
                } else if (strikethrough.contains(i)) { // check if we are swapping strikethrough state
                    isStrikethrough = !isStrikethrough;
                    i++; // skip second tilde
                } else if (italics.contains(i)) { // check if we are swapping italics state
                    isItalicized = !isItalicized;
                }
            } else {
                builder.append(arr[i]); // append the next character to the buffer
            }
        }
        if (!builder.isEmpty()) { // if there is remaining text, add it to the flow
            append(flow, isBold, isItalicized, isStrikethrough, builder);
        }
    }

    /**
     * Appends text with the given font properties to a TextFlow
     *
     * @param flow            the text flow to append to
     * @param isBold          whether the given text should be bolded
     * @param isItalicized    whether the given text should be italicized
     * @param isStrikethrough whether the given text should have strikethrough
     * @param builder         the text
     */
    private static void append(TextFlow flow, boolean isBold, boolean isItalicized, boolean isStrikethrough, StringBuilder builder) {
        Text txt = new Text(builder.toString());
        txt.strikethroughProperty().set(isStrikethrough);
        txt.setFont(Font.font(Font.getDefault().getFamily(), isBold ? FontWeight.BOLD : FontWeight.NORMAL, isItalicized ? FontPosture.ITALIC : FontPosture.REGULAR, Font.getDefault().getSize()));
        flow.getChildren().add(txt);
    }
}
