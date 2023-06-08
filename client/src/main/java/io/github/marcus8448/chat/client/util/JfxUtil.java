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

import io.github.marcus8448.chat.client.config.Account;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.Locale;

public class JfxUtil {
    public static final int BUTTON_HEIGHT = 25;
    public static final int BUTTON_WIDTH = 70;
    public static final StringConverter<Account> ACCOUNT_STRING_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(Account object) {
            return object == null ? "" : object.username() + " [" + JfxUtil.keyId(object.publicKey().getEncoded()) + "]";
        }

        @Override
        public Account fromString(String string) {
            return null;
        }
    };

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
        int len = key.length - 10;
        return (Integer.toHexString(key[len - 8] << 24 | (key[len - 7] & 0xFF) << 16 | (key[len - 6] & 0xFF) << 8 | key[len - 5] & 0xFF) + Integer.toHexString(key[len - 4] << 24 | (key[len - 3] & 0xFF) << 16 | (key[len - 2] & 0xFF) << 8 | key[len - 1] & 0xFF)).toUpperCase(Locale.ROOT);
    }
}
