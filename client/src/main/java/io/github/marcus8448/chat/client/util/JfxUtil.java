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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class JfxUtil {
    public static final Insets PADDING_CORE = new Insets(8, 10, 8, 10);
    public static final double SPACING = 10;
    public static final int CONTROL_HEIGHT = 25;
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
    private static final Text TEXT_HOLDER = new Text();

    public static void buttonPressCallback(Node button, Runnable r) {
        button.setOnKeyPressed(enterKeyCallback(r));
        button.setOnMouseClicked(e -> Platform.runLater(r));
    }

    public static void unescapedEnterCallback(Node field, Runnable r) {
        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                Platform.runLater(r);
            }
        });
    }

    public static EventHandler<? super KeyEvent> enterKeyCallback(Runnable r) {
        return e -> {
            if (e.getCode() == KeyCode.ENTER) {
                Platform.runLater(r); // cannot run directly - JVM crash on linux
            }
        };
    }

    public static HBox createInputRow(Label label, TextField field, String prompt, double labelLen) {
        label.setPrefWidth(labelLen);

        field.setPromptText(prompt);
        field.setPrefHeight(JfxUtil.CONTROL_HEIGHT);

        HBox row = new HBox(label, field); // display: "<label> [input field]"
        setupRow(label, field, row);
        return row;
    }

    public static HBox createComboInputRow(Label label, ComboBox<?> field, double labelLen) {
        if (labelLen <= 0) {
            labelLen = getTextWidth(label.getText());
        }
        label.setMinWidth(labelLen);

        field.setPrefHeight(JfxUtil.CONTROL_HEIGHT);
        field.setPrefWidth(Integer.MAX_VALUE);

        HBox row = new HBox(label, field); // display: "<label> [combo box]"
        setupRow(label, field, row);
        return row;
    }

    public static HBox createButtonRow(Control... buttons) { // NULL = spacing
        HBox row = new HBox();
        for (Control control : buttons) {
            if (control != null) {
                control.setPrefHeight(CONTROL_HEIGHT);
                HBox.setHgrow(control, Priority.NEVER);
                row.getChildren().add(control);
            } else {
                Pane spacing = new Pane();
                HBox.setHgrow(spacing, Priority.ALWAYS);
                row.getChildren().add(spacing);
            }
        }
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(SPACING);
        VBox.setVgrow(row, Priority.NEVER);
        return row;
    }

    public static void setupFailureLabel(Label label) {
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setPrefWidth(Integer.MAX_VALUE);
        label.setTextFill(JfxUtil.FAILURE_COLOUR);
        VBox.setVgrow(label, Priority.NEVER);
    }

    private static void setupRow(Node label, Node field, HBox hBox) {
        hBox.setSpacing(SPACING);
        hBox.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(label, Priority.NEVER);
        HBox.setHgrow(field, Priority.ALWAYS);
        VBox.setVgrow(hBox, Priority.NEVER);
    }

    public static double getTextWidth(String text) {
        TEXT_HOLDER.setText(text);
        return TEXT_HOLDER.getBoundsInLocal().getWidth();
    }

    public static void initializePadding(VBox vBox) {
        vBox.setPadding(JfxUtil.PADDING_CORE);
        vBox.setSpacing(JfxUtil.SPACING);
    }

    public static Pane createSpacing() {
        Pane pane = new Pane();
        pane.setPrefHeight(0);
        pane.setPadding(Insets.EMPTY);
        VBox.setVgrow(pane, Priority.ALWAYS);
        return pane;
    }

    public static void resizeAutoHeight(Stage stage, Scene scene, double width) {
        stage.setResizable(true);
        stage.setScene(scene);
        stage.sizeToScene();
        Platform.runLater(() -> {
            stage.setWidth(width);
            stage.centerOnScreen();
        });
    }
}
