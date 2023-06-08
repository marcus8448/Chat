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

package io.github.marcus8448.chat.client.util;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Locale;

public class JfxUtil {
    public static final int BUTTON_HEIGHT = 25;
    public static final int BUTTON_WIDTH = 70;

    public static void buttonPressCallback(Node button, Runnable r) {
        button.setOnKeyPressed(enterKeyCallback(r));
        button.setOnMouseClicked(e -> r.run());
    }
    public static EventHandler<? super KeyEvent> enterKeyCallback(Runnable r) {
        return e -> {
            if (e.getCode() == KeyCode.ENTER) {
                r.run();
            }
        };
    }

    public static String keyId(byte[] key) {
        return (Integer.toHexString(key[0] << 24 | (key[1] & 0xFF) << 16 | (key[2] & 0xFF) << 8 | key[3] & 0xFF) + Integer.toHexString(key[4] << 24 | (key[5] & 0xFF) << 16 | (key[6] & 0xFF) << 8 | key[7] & 0xFF)).toUpperCase(Locale.ROOT);
    }
}
