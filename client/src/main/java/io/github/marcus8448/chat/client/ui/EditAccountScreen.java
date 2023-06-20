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
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.misc.Result;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

/**
 * Screen that allows users to change their username and/or password
 */
public class EditAccountScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String INVALID_USERNAME = "Invalid username: %s";
    private static final String INVALID_PASSWORD = "Invalid password: %s";

    private static final String PASSWORD_NOT_MATCHING = "Passwords do not match";
    /**
     * The client instance
     */
    private final Client client;
    /**
     * Account selection dropdown
     */
    private final ComboBox<Account> accounts;
    /**
     * Input field for username change
     */
    private final TextField username = new TextField();
    /**
     * Input field for current password
     */
    private final PasswordField passwordField = new PasswordField();
    /**
     * Checkbox of whether to change passwords
     */
    private final CheckBox changePassword = new CheckBox("Change password?");
    /**
     * The new password input
     */
    private final PasswordField newPasswordField = new PasswordField();
    /**
     * The secondary new password input (must match the first)
     */
    private final PasswordField newPasswordField2 = new PasswordField();
    /**
     * Displays why account editing may have failed
     */
    private final Label failureReason = new Label();
    /**
     * The current stage (probably not primary)
     */
    private final Stage stage;

    public EditAccountScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;
        this.accounts = new ComboBox<>(client.config.getAccounts());
        this.accounts.setConverter(JfxUtil.ACCOUNT_STRING_CONVERTER);

        VBox root = new VBox();
        JfxUtil.initializePadding(root);

        this.accounts.getSelectionModel().selectedItemProperty().addListener((ChangeListener<? super Account>) (o, old, newValue) -> {
            if (newValue == null) {
                this.username.setPromptText("");
            } else {
                if (this.username.getText().isBlank()) this.username.setText(newValue.username());
                this.username.setPromptText(newValue.username());
            }
        });

        double len = JfxUtil.getTextWidth("Username");

        root.getChildren().add(JfxUtil.createComboInputRow(new Label("Account"), this.accounts, len));
        root.getChildren().add(JfxUtil.createInputRow(new Label("Username"), this.username, "example", len));
        root.getChildren().add(JfxUtil.createInputRow(new Label("Password"), this.passwordField, "password", len));


        // whether to change the password of the account
        VBox.setVgrow(this.changePassword, Priority.NEVER);
        this.changePassword.setPadding(new Insets(6));
        root.getChildren().add(this.changePassword);

        root.getChildren().add(JfxUtil.createInputRow(new Label("New Password"), this.newPasswordField, "password", -1));
        root.getChildren().add(JfxUtil.createInputRow(new Label("New Password (again)"), this.newPasswordField2, "password (repeat)", -1));

        this.changePassword.setOnAction(e -> {
            if (this.changePassword.isSelected()) { // enable/disable new password fields based on state
                this.newPasswordField.setDisable(false);
                this.newPasswordField2.setDisable(false);
            } else {
                this.newPasswordField.setDisable(true);
                this.newPasswordField2.setDisable(true);
            }
        });
        this.newPasswordField.setDisable(true);
        this.newPasswordField2.setDisable(true);

        JfxUtil.setupFailureLabel(this.failureReason);
        root.getChildren().add(this.failureReason);

        root.getChildren().add(JfxUtil.zeroSpacing());

        Button delete = new Button("Delete");
        Button cancel = new Button("Cancel");
        Button save = new Button("Update");
        JfxUtil.buttonPressCallback(cancel, stage::close);
        JfxUtil.buttonPressCallback(delete, this::deleteAccount);
        JfxUtil.buttonPressCallback(save, this::updateAccount);
        root.getChildren().add(JfxUtil.createButtonRow(cancel, delete, null, save));

        // set up the scene and stage
        Scene scene = new Scene(root);

        stage.setWidth(400);
        stage.setHeight(330);
        stage.setResizable(true);
        stage.setTitle("Edit Account");
        stage.setScene(scene);
    }

    /**
     * Prompts user to verify account deletion request (and deletes it if so)
     */
    private void deleteAccount() {
        if (this.accounts.getSelectionModel().isEmpty()) {
            this.failureReason.setText("You must select an account");
            return;
        }
        Account selectedItem = this.accounts.getSelectionModel().getSelectedItem(); // get selected account

        // prompt user to confirm
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you wish to delete " + JfxUtil.ACCOUNT_STRING_CONVERTER.toString(selectedItem), ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) { // check that they say YES, or cancel by default
            this.accounts.getSelectionModel().clearSelection(); // deselect account
            this.client.config.removeAccount(selectedItem); // delete account
            this.stage.close(); // close window
        }
    }

    /**
     * Updates the selected account with the new values
     */
    private void updateAccount() {
        if (this.accounts.getSelectionModel().isEmpty()) { // verify that an account is selected
            this.failureReason.setText("You must select an account");
            return;
        }

        Account selectedAccount = this.accounts.getSelectionModel().getSelectedItem();

        // verify that the username is valid
        Result<String, String> res = ParseUtil.validateUsername(this.username.getText());
        if (res.isError()) {
            this.failureReason.setText(INVALID_USERNAME.formatted(res.unwrapError()));
            return;
        }
        String username = res.unwrap();

        SecretKey passKey; // AES config encryption key
        String oldPassword = this.passwordField.getText(); // the current password text
        String newPassword; // new password (== old password if unchanged)
        if (this.changePassword.isSelected()) { // check if the password is going to be changed
            String text = this.newPasswordField.getText();
            var result = ParseUtil.validatePassword(text); // verify that the password is valid
            if (result.isError()) {
                this.failureReason.setText(INVALID_PASSWORD.formatted(result.unwrapError()));
                return;
            }
            if (!text.equals(this.newPasswordField2.getText())) { // verify that the repeated password is the same
                this.failureReason.setText(PASSWORD_NOT_MATCHING);
                return;
            }
            newPassword = text;
        } else {
            newPassword = oldPassword; // no change.
        }

        try {
            // create an AES key based on the username and current password, for config decryption
            passKey = CryptoHelper.generateUserPassKey(oldPassword.toCharArray(), selectedAccount.username());
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Failed to calculate password hash.");
            LOGGER.error("PBKDF2 key derivation failure", e);
            return;
        }

        Cipher aesCipher = CryptoHelper.createAesCipher(); // get an AES cipher instance
        try {
            // initialize the cipher to decrypt the config data
            aesCipher.init(Cipher.DECRYPT_MODE, passKey);
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher.");
            LOGGER.error("AES cipher initialization failed", e);
            return;
        }

        AccountData accountData;
        try {
            // decrypt the account data using the user/pass key
            accountData = selectedAccount.data().decrypt(aesCipher);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
            this.failureReason.setText("Incorrect password");
            LOGGER.error("AES data decryption failed", e);
            return;
        }

        try {
            // generate another secret key for encryption using the new password/username
            passKey = CryptoHelper.generateUserPassKey(newPassword.toCharArray(), username);
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Failed to calculate password hash.");
            LOGGER.error("PBKDF2 key derivation failure", e);
            return;
        }

        try {
            // re-initialize the cipher for encryption with the new key
            aesCipher.init(Cipher.ENCRYPT_MODE, passKey);
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher.");
            LOGGER.error("AES cipher initialization failed", e);
            return;
        }

        AccountData.EncryptedAccountData newData;
        try {
            // encrypt the data.
            newData = accountData.encrypt(aesCipher);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            this.failureReason.setText("Failed to encrypt account data.");
            LOGGER.error("Failed to encrypt account data", e);
            return;
        }

        // deselect the account
        this.accounts.getSelectionModel().clearSelection();
        // create a new account with the new data
        Account account = new Account(username, selectedAccount.publicKey(), newData);
        // add the account
        this.client.config.addAccount(account);
        // remove the old account
        this.client.config.removeAccount(selectedAccount);
        // select the new account
        this.accounts.getSelectionModel().select(account);
        // close the window
        this.stage.close();
    }
}
