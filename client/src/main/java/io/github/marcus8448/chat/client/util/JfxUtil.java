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
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class JfxUtil {
    public static final int BUTTON_HEIGHT = 25;
    public static final int BUTTON_WIDTH = 70;
    public static final StringConverter<Account> ACCOUNT_STRING_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(Account object) {
            return object == null ? "" : object.username() + " [" + CryptoHelper.sha256Hash(object.publicKey().getEncoded()) + "]";
        }

        @Override
        public Account fromString(String string) {
            return null;
        }
    };
    public static final Paint LINK_COLOUR = Paint.valueOf("#21a7ff");
    public static final Paint FAILURE_COLOUR = Paint.valueOf("#ee1100");
    public static final Paint NOT_VERIFIED_COLOUR = Paint.valueOf("#e87474");

    public static void buttonPressCallback(Node button, Runnable r) {
        button.setOnKeyPressed(enterKeyCallback(r));
        button.setOnMouseClicked(e -> r.run());
    }
    public static EventHandler<? super KeyEvent> enterKeyCallback(Runnable r) {
        return e -> {
            if (e.getCode() == KeyCode.ENTER) {
                Platform.runLater(r); // cannot run directly - JVM crash on linux
            }
        };
    }

    private static final Text TEXT_HOLDER = new Text();
    public static double getTextWidth(String text) {
        TEXT_HOLDER.setText(text);
        return TEXT_HOLDER.getBoundsInLocal().getWidth();
    }
}
