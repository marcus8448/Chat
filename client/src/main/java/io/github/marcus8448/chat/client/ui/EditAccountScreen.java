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
import io.github.marcus8448.chat.client.config.Account;
import io.github.marcus8448.chat.client.util.JfxUtil;
import io.github.marcus8448.chat.client.util.ParseUtil;
import io.github.marcus8448.chat.core.Result;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class EditAccountScreen {
    private static final String INVALID_USERNAME = "Invalid username: %s";
    private static final String INVALID_PASSWORD = "Invalid password: %s";

    private static final int PADDING = 12;
    private static final int BUTTON_HEIGHT = 25;

    private final Client client;
    private final ComboBox<Account> accounts;
    private final TextField username = new TextField();
    private final Label failureReason = new Label();
    private final Stage stage;

    public EditAccountScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;
        this.accounts = new ComboBox<>(client.config.getAccounts());
        this.accounts.setConverter(JfxUtil.ACCOUNT_STRING_CONVERTER);

        VBox vBox = new VBox();
        Insets paddingH = new Insets(0, PADDING, 0, PADDING);
        Insets paddingCore = new Insets(PADDING / 2.0, PADDING, PADDING / 2.0, PADDING);

        Label accountLabel = new Label("Account");
        accountLabel.setPadding(paddingH);
        accountLabel.setMinWidth(90);
        this.accounts.setMaxWidth(1289908123);
        this.accounts.setPadding(paddingH);
        this.accounts.setPrefHeight(25);
        this.username.setText("");
        this.username.setPromptText("");
        this.accounts.getSelectionModel().selectedItemProperty().addListener((ChangeListener<? super Account>) (o, old, newValue) -> {
            if (newValue == null) {
                this.username.setDisable(true);
                this.username.setText("");
                this.username.setPromptText("");
            } else {
                this.username.setDisable(false);
                this.username.setText(newValue.username());
                this.username.setPromptText(newValue.username());
            }
        });

        HBox accountSelection = new HBox(accountLabel, this.accounts);
        accountSelection.setPadding(new Insets(PADDING, PADDING, PADDING / 2.0, PADDING));
        HBox.setHgrow(accountLabel, Priority.NEVER);
        HBox.setHgrow(this.accounts, Priority.ALWAYS);
        VBox.setVgrow(accountSelection, Priority.NEVER);
        vBox.getChildren().add(accountSelection);

        Label usernameLabel = new Label("Username");
        usernameLabel.setPadding(paddingH);
        this.username.setPromptText("example");
        this.username.setMaxWidth(1289908123);
        this.username.setPadding(paddingH);
        this.username.setPrefHeight(25);

        HBox usernameChange = new HBox(usernameLabel, this.username);
        usernameChange.setPadding(new Insets(PADDING, PADDING, PADDING / 2.0, PADDING));
        HBox.setHgrow(usernameLabel, Priority.NEVER);
        HBox.setHgrow(this.username, Priority.ALWAYS);
        VBox.setVgrow(usernameChange, Priority.NEVER);
        vBox.getChildren().add(usernameChange);

        this.failureReason.setAlignment(Pos.CENTER_RIGHT);
        this.failureReason.setPrefWidth(10000);
        this.failureReason.setPadding(paddingH);
        this.failureReason.setTextFill(Paint.valueOf("#ee1100"));
        VBox.setVgrow(this.failureReason, Priority.NEVER);
        vBox.getChildren().add(this.failureReason);

        AnchorPane spacing = new AnchorPane();
        VBox.setVgrow(spacing, Priority.ALWAYS);
        vBox.getChildren().add(spacing);

        AnchorPane spacing2 = new AnchorPane();
        Button delete = new Button("Delete");
        delete.setPrefHeight(BUTTON_HEIGHT);
        delete.setPadding(paddingH);
        Button cancel = new Button("Cancel");
        cancel.setPrefHeight(BUTTON_HEIGHT);
        cancel.setPadding(paddingH);
        Button save = new Button("Update");
        save.setPrefHeight(BUTTON_HEIGHT);
        save.setPadding(paddingH);
        JfxUtil.buttonPressCallback(cancel, stage::close);
        JfxUtil.buttonPressCallback(delete, this::deleteAccount);
        JfxUtil.buttonPressCallback(save, this::updateAccount);

        HBox buttons = new HBox(cancel, spacing2, save);
        buttons.setPadding(paddingCore);
        HBox.setHgrow(spacing2, Priority.ALWAYS);
        HBox.setHgrow(cancel, Priority.NEVER);
        HBox.setHgrow(save, Priority.NEVER);
        VBox.setVgrow(buttons, Priority.NEVER);
        vBox.getChildren().add(buttons);

        Scene scene = new Scene(vBox);

        stage.setWidth(400);
        stage.setHeight(175);
        stage.setResizable(true);
        stage.setScene(scene);
    }

    private void deleteAccount() {
        if (this.accounts.getSelectionModel().isEmpty()) {
            this.failureReason.setText("You must select an account");
            return;
        }
        Account selectedItem = this.accounts.getSelectionModel().getSelectedItem();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you wish to delete " + JfxUtil.ACCOUNT_STRING_CONVERTER.toString(selectedItem), ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            this.accounts.getSelectionModel().clearSelection();
            this.client.config.removeAccount(selectedItem);
            this.stage.close();
        }
    }

    private void updateAccount() {
        if (this.accounts.getSelectionModel().isEmpty()) {
            this.failureReason.setText("You must select an account");
            return;
        }

        Result<String, String> res = ParseUtil.validateUsername(this.username.getText());
        if (res.isError()) {
            this.failureReason.setText(INVALID_USERNAME.formatted(res.unwrapError()));
            return;
        }
        String username = res.unwrap();

        Account selectedItem = this.accounts.getSelectionModel().getSelectedItem();
        this.accounts.getSelectionModel().clearSelection();

        Account account = new Account(username, selectedItem.privateKey(), selectedItem.publicKey());
        this.client.config.addAccount(account);
        this.client.config.removeAccount(selectedItem);
        this.accounts.getSelectionModel().select(account);

        this.stage.close();
    }
}
