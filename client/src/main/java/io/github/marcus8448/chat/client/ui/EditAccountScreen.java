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
import io.github.marcus8448.chat.client.config.AccountData;
import io.github.marcus8448.chat.client.util.JfxUtil;
import io.github.marcus8448.chat.client.util.ParseUtil;
import io.github.marcus8448.chat.core.Result;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

public class EditAccountScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String INVALID_USERNAME = "Invalid username: %s";
    private static final String INVALID_PASSWORD = "Invalid password: %s";

    private static final int PADDING = 12;
    private static final int BUTTON_HEIGHT = 25;
    private static final String PASSWORD_NOT_MATCHING = "Passwords do not match";

    private final Client client;
    private final ComboBox<Account> accounts;
    private final TextField username = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final CheckBox changePassword = new CheckBox("Change password?");
    private final PasswordField newPasswordField = new PasswordField();
    private final PasswordField newPasswordField2 = new PasswordField();
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
        Insets padding = new Insets(PADDING, PADDING, PADDING / 2.0, PADDING);
        accountSelection.setPadding(padding);
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
        usernameChange.setPadding(padding);
        HBox.setHgrow(usernameLabel, Priority.NEVER);
        HBox.setHgrow(this.username, Priority.ALWAYS);
        VBox.setVgrow(usernameChange, Priority.NEVER);
        vBox.getChildren().add(usernameChange);

        Label passwordLabel = new Label("Password");
        passwordLabel.setPadding(paddingH);
        this.passwordField.setPromptText("Password");
        this.passwordField.setMaxWidth(1289908123);
        this.passwordField.setPadding(paddingH);
        this.passwordField.setPrefHeight(25);

        HBox password = new HBox(passwordLabel, this.passwordField);
        password.setPadding(padding);
        HBox.setHgrow(passwordLabel, Priority.NEVER);
        HBox.setHgrow(this.passwordField, Priority.ALWAYS);
        VBox.setVgrow(password, Priority.NEVER);
        vBox.getChildren().add(password);

        Label newPasswordLabel = new Label("New Password");
        newPasswordLabel.setPadding(paddingH);
        this.newPasswordField.setPromptText("Password");
        this.newPasswordField.setMaxWidth(1289908123);
        this.newPasswordField.setPadding(paddingH);
        this.newPasswordField.setPrefHeight(25);

        HBox newPassword = new HBox(newPasswordLabel, this.newPasswordField);
        newPassword.setPadding(padding);
        HBox.setHgrow(newPasswordLabel, Priority.NEVER);
        HBox.setHgrow(this.newPasswordField, Priority.ALWAYS);
        VBox.setVgrow(newPassword, Priority.NEVER);
        VBox.setVgrow(this.changePassword, Priority.NEVER);
        vBox.getChildren().add(this.changePassword);
        this.changePassword.setPadding(padding);
        vBox.getChildren().add(newPassword);

        Label newPasswordLabel2 = new Label("New Password (again)");
        newPasswordLabel2.setPadding(paddingH);
        this.newPasswordField2.setPromptText("Password (repeat)");
        this.newPasswordField2.setMaxWidth(1289908123);
        this.newPasswordField2.setPadding(paddingH);
        this.newPasswordField2.setPrefHeight(25);

        HBox newPassword2 = new HBox(newPasswordLabel2, this.newPasswordField2);
        newPassword2.setPadding(padding);
        HBox.setHgrow(newPasswordLabel2, Priority.NEVER);
        HBox.setHgrow(this.newPasswordField2, Priority.ALWAYS);
        VBox.setVgrow(newPassword2, Priority.NEVER);
        vBox.getChildren().add(newPassword2);

        this.changePassword.setOnAction(e -> {
            if (this.changePassword.isSelected()) {
                newPassword.setDisable(false);
                newPassword2.setDisable(false);
            } else {
                newPassword.setDisable(true);
                newPassword2.setDisable(true);
            }
        });
        newPassword.setDisable(true);
        newPassword2.setDisable(true);

        this.failureReason.setAlignment(Pos.CENTER_RIGHT);
        this.failureReason.setPrefWidth(10000);
        this.failureReason.setPadding(paddingH);
        this.failureReason.setTextFill(JfxUtil.FAILURE_COLOUR);
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

        HBox buttons = new HBox(cancel, delete, spacing2, save);
        buttons.setPadding(paddingCore);
        HBox.setHgrow(spacing2, Priority.ALWAYS);
        HBox.setHgrow(cancel, Priority.NEVER);
        HBox.setHgrow(save, Priority.NEVER);
        VBox.setVgrow(buttons, Priority.NEVER);
        vBox.getChildren().add(buttons);

        Scene scene = new Scene(vBox);

        stage.setWidth(400);
        stage.setHeight(350);
        stage.setResizable(true);
        stage.setTitle("Edit Account");
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
        Account selectedAccount = this.accounts.getSelectionModel().getSelectedItem();

        Result<String, String> res = ParseUtil.validateUsername(this.username.getText());
        if (res.isError()) {
            this.failureReason.setText(INVALID_USERNAME.formatted(res.unwrapError()));
            return;
        }
        String username = res.unwrap();

        SecretKey secretKey;
        String oldPassword = this.passwordField.getText();
        String newPassword;
        if (this.changePassword.isSelected()) {
            String text = this.newPasswordField.getText();
            var result = ParseUtil.validatePassword(text);
            if (result.isError()) {
                this.failureReason.setText(INVALID_PASSWORD.formatted(result.unwrapError()));
                return;
            }
            if (!text.equals(this.newPasswordField2.getText())) {
                this.failureReason.setText(PASSWORD_NOT_MATCHING);
                return;
            }
            newPassword = text;
        } else {
            newPassword = oldPassword;
        }

        try {
            secretKey = new SecretKeySpec(CryptoHelper.PBKDF2_SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(oldPassword.toCharArray(), selectedAccount.username().getBytes(StandardCharsets.UTF_8), 65536, 256)).getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Failed to calculate password hash.");
            LOGGER.error("PBKDF2 key derivation failure", e);
            return;
        }

        Cipher aesCipher = CryptoHelper.createAesCipher();
        try {
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher.");
            LOGGER.error("AES cipher initialization failed", e);
            return;
        }

        AccountData decrypt;
        try {
            decrypt = selectedAccount.data().decrypt(aesCipher);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
            this.failureReason.setText("Incorrect password");
            LOGGER.error("AES data decryption failed", e);
            return;
        }

        try {
            secretKey = new SecretKeySpec(CryptoHelper.PBKDF2_SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(newPassword.toCharArray(), username.getBytes(StandardCharsets.UTF_8), 65536, 256)).getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Failed to calculate password hash.");
            LOGGER.error("PBKDF2 key derivation failure", e);
            return;
        }

        try {
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher.");
            LOGGER.error("AES cipher initialization failed", e);
            return;
        }

        AccountData.EncryptedAccountData newData;
        try {
            newData = decrypt.encrypt(aesCipher);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            this.failureReason.setText("Failed to encrypt account data.");
            LOGGER.error("Failed to encrypt account data", e);
            return;
        }

        this.accounts.getSelectionModel().clearSelection();
        Account account = new Account(username, selectedAccount.publicKey(), newData);
        this.client.config.addAccount(account);
        this.client.config.removeAccount(selectedAccount);
        this.accounts.getSelectionModel().select(account);

        this.stage.close();
    }
}
