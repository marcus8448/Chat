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
import org.jetbrains.annotations.ApiStatus;

/**
 * JFX boilerplate code and other constants
 */
public class JfxUtil {
    /**
     * Base padding for input rows
     */
    public static final Insets PADDING_CORE = new Insets(8, 10, 8, 10);
    /**
     * The spacing between V/H box items
     */
    public static final double SPACING = 10;
    /**
     * Height for control items
     */
    public static final int CONTROL_HEIGHT = 25;
    /**
     * Width of buttons
     */
    public static final int BUTTON_WIDTH = 70;
    /**
     * Converts accounts to strings
     */
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
    /**
     * Colour to use for "links"
     */
    public static final Paint LINK_COLOUR = Paint.valueOf("#21a7ff");
    /**
     * Colour of failure text
     */
    public static final Paint FAILURE_COLOUR = Paint.valueOf("#ee1100");
    /**
     * Colour to use for unverified messages
     */
    public static final Paint NOT_VERIFIED_COLOUR = Paint.valueOf("#e87474");
    @ApiStatus.Internal
    private static final Text TEXT_HOLDER = new Text();

    /**
     * Runs the given code when the button is clicked or selected and pressed enter on
     *
     * @param button the button to active the code
     * @param r      the code to run
     */
    public static void buttonPressCallback(Node button, Runnable r) {
        button.setOnKeyPressed(enterKeyCallback(r));
        button.setOnMouseClicked(e -> Platform.runLater(r));
    }

    /**
     * Runs the given code when the enter key is pressed WITHOUT shift down
     *
     * @param field the node that must be selected to activate
     * @param r     the code to run
     */
    public static void unescapedEnterCallback(Node field, Runnable r) {
        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                Platform.runLater(r);
            }
        });
    }

    /**
     * Runs the given code when the enter key is pressed
     *
     * @param r the event
     * @return the handler that will run the code
     */
    public static EventHandler<? super KeyEvent> enterKeyCallback(Runnable r) {
        return e -> {
            if (e.getCode() == KeyCode.ENTER) {
                Platform.runLater(r); // cannot run directly - JVM crash on linux
            }
        };
    }

    /**
     * Creates a [label] [input] row for GUIs
     *
     * @param label    the label to use
     * @param field    the input field to use
     * @param prompt   the prompt for the input field
     * @param labelLen the length to align the label to
     * @return the new row with the given label and input field
     */
    public static HBox createInputRow(Label label, TextField field, String prompt, double labelLen) {
        label.setPrefWidth(labelLen);

        field.setPromptText(prompt);
        field.setPrefHeight(JfxUtil.CONTROL_HEIGHT);

        HBox row = new HBox(label, field); // display: "<label> [input field]"
        setupRow(label, field, row);
        return row;
    }

    /**
     * Creates a [label] [box] row for GUIs
     *
     * @param label    the label to use
     * @param field    the combo box to use
     * @param labelLen the length to align the label to
     * @return the new row with the given label and combo box
     * @see #createInputRow(Label, TextField, String, double)
     */
    public static HBox createComboInputRow(Label label, ComboBox<?> field, double labelLen) {
        if (labelLen <= 0) {
            labelLen = getTextWidth(label.getText());
        }
        label.setMinWidth(labelLen);

        field.setPrefHeight(JfxUtil.CONTROL_HEIGHT);
        field.setMaxWidth(Integer.MAX_VALUE);

        HBox row = new HBox(label, field); // display: "<label> [combo box]"
        setupRow(label, field, row);
        return row;
    }

    /**
     * Creates a row of buttons
     *
     * @param buttons the buttons to add. {@code null} inserts a spacer
     * @return the new row of buttons
     */
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

    /**
     * Sets the properties for a failure reason label.
     *
     * @param label the label to set up
     */
    public static void setupFailureLabel(Label label) {
        label.setAlignment(Pos.CENTER_RIGHT); // right aligned
        label.setPrefWidth(Integer.MAX_VALUE); // needed to stretch across the screen for alignment
        label.setTextFill(JfxUtil.FAILURE_COLOUR); // red text
        VBox.setVgrow(label, Priority.NEVER); // do not expand down (single row)
    }

    @ApiStatus.Internal
    private static void setupRow(Node label, Node field, HBox hBox) {
        hBox.setSpacing(SPACING); // set spacing between elements
        hBox.setAlignment(Pos.CENTER_LEFT); // keep text centered

        HBox.setHgrow(label, Priority.NEVER); // don't expand the label
        HBox.setHgrow(field, Priority.ALWAYS); // expand the input field
        VBox.setVgrow(hBox, Priority.NEVER); // don't expand vertically
    }

    /**
     * @param text the text to get the width of
     * @return the width of the given text (default font)
     */
    public static double getTextWidth(String text) {
        TEXT_HOLDER.setText(text);
        return TEXT_HOLDER.getBoundsInLocal().getWidth();
    }

    /**
     * Sets the padding and spacing for a vBox
     *
     * @param vBox the vBox to set the padding and spacing of
     */
    public static void initVbox(VBox vBox) {
        vBox.setPadding(JfxUtil.PADDING_CORE);
        vBox.setSpacing(JfxUtil.SPACING);
    }

    /**
     * Creates an empty pane to use as spacing in a V/H box
     *
     * @return a new pane
     */
    public static Pane createSpacing() {
        Pane pane = new Pane();
        pane.setPrefHeight(0);
        pane.setPadding(Insets.EMPTY);
        VBox.setVgrow(pane, Priority.ALWAYS);
        return pane;
    }

    /**
     * Resizes the stage to meet the height requirements of the window, while keeping a fixed width
     *
     * @param stage the stage to resize
     * @param scene the scene of the stage
     * @param width the target width of the stage
     */
    public static void resizeAutoHeight(Stage stage, Scene scene, double width) {
        stage.setResizable(true);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setWidth(width + 1);
        Platform.runLater(() -> {
            stage.setMinWidth(width);
            stage.setMinHeight(stage.getHeight());
            stage.setWidth(width);
            stage.centerOnScreen();
            stage.show();
        });
    }
}
