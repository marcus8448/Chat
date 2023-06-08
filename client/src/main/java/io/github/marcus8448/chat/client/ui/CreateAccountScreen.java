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

package io.github.marcus8448.chat.client.ui;

import io.github.marcus8448.chat.client.Client;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class CreateAccountScreen {
    private static final int CONTENT_HEIGHT = 192 - 25;

    private static final int PADDING = 12;
    private static final int BUTTON_HEIGHT = 25;
    private static final int BUTTON_WIDTH = 70;

    public CreateAccountScreen(Client client, Stage stage) {
        VBox vBox = new VBox();
        Insets paddingH = new Insets(0, PADDING, 0, PADDING);
        Insets paddingCore = new Insets(PADDING / 2.0, PADDING, PADDING / 2.0, PADDING);

        Label accountLabel = new Label("Username");
        accountLabel.setPadding(paddingH);
        TextField server = new TextField();
        server.setMaxWidth(1289908123);
        server.setPadding(paddingH);

        HBox accountSelection = new HBox(accountLabel, server);
        accountSelection.setPadding(new Insets(PADDING, PADDING, PADDING / 2.0, PADDING));
        HBox.setHgrow(accountLabel, Priority.NEVER);
        HBox.setHgrow(server, Priority.ALWAYS);
        VBox.setVgrow(accountSelection, Priority.NEVER);
        vBox.getChildren().add(accountSelection);

        Label passwordLabel = new Label("Password");
        passwordLabel.setPadding(paddingH);
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefHeight(25);
        passwordField.setPadding(paddingH);

        HBox passwordInput = new HBox(passwordLabel, passwordField);
        passwordInput.setPadding(paddingCore);
        HBox.setHgrow(passwordLabel, Priority.NEVER);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        VBox.setVgrow(passwordInput, Priority.NEVER);
        vBox.getChildren().add(passwordInput);

        AnchorPane spacing = new AnchorPane();
        VBox.setVgrow(spacing, Priority.ALWAYS);
        vBox.getChildren().add(spacing);

        Label createAccountPrompt = new Label("No account? Create one!");
        createAccountPrompt.setTextFill(Paint.valueOf("#21a7ff"));
        createAccountPrompt.setAlignment(Pos.CENTER);
        AnchorPane spacing2 = new AnchorPane();
        Button cancel = new Button("Cancel");
        cancel.setPrefWidth(BUTTON_WIDTH);
        cancel.setPrefHeight(BUTTON_HEIGHT);
        cancel.setPadding(paddingH);
        Button login = new Button("Login");
        login.setPrefWidth(BUTTON_WIDTH);
        login.setPrefHeight(BUTTON_HEIGHT);
        login.setPadding(paddingH);

        HBox buttons = new HBox(createAccountPrompt, spacing2, cancel, login);
        buttons.setPadding(paddingCore);
        HBox.setHgrow(createAccountPrompt, Priority.NEVER);
        HBox.setHgrow(spacing2, Priority.ALWAYS);
        HBox.setHgrow(cancel, Priority.NEVER);
        HBox.setHgrow(login, Priority.NEVER);
        VBox.setVgrow(buttons, Priority.NEVER);
        vBox.getChildren().add(buttons);

        Scene scene = new Scene(vBox);

        stage.setWidth(400);
        stage.setHeight(200);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
    }
}
