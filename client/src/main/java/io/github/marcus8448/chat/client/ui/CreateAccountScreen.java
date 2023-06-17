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
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

public class CreateAccountScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String INVALID_USERNAME = "Invalid username: %s";
    private static final String INVALID_PASSWORD = "Invalid password: %s";

    private static final int PADDING = 12;
    private static final int BUTTON_HEIGHT = 25;

    private final Client client;
    private final PasswordField passwordField = new PasswordField();
    private final TextField username = new TextField();
    private final Label failureReason = new Label();
    private final Stage stage;

    public CreateAccountScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;
        VBox vBox = new VBox();
        Insets paddingH = new Insets(0, PADDING, 0, PADDING);
        Insets paddingCore = new Insets(PADDING / 2.0, PADDING, PADDING / 2.0, PADDING);

        Label accountLabel = new Label("Username");
        accountLabel.setPadding(paddingH);
        this.username.setPromptText("example");
        this.username.setMaxWidth(1289908123);
        this.username.setPadding(paddingH);
        this.username.setPrefHeight(25);

        HBox accountSelection = new HBox(accountLabel, this.username);
        accountSelection.setPadding(new Insets(PADDING, PADDING, PADDING / 2.0, PADDING));
        HBox.setHgrow(accountLabel, Priority.NEVER);
        HBox.setHgrow(this.username, Priority.ALWAYS);
        VBox.setVgrow(accountSelection, Priority.NEVER);
        vBox.getChildren().add(accountSelection);

        Label passwordLabel = new Label("Password ");
        passwordLabel.setPadding(paddingH);
        this.passwordField.setPrefHeight(25);
        this.passwordField.setPadding(paddingH);

        HBox passwordInput = new HBox(passwordLabel, this.passwordField);
        passwordInput.setPadding(paddingCore);
        HBox.setHgrow(passwordLabel, Priority.NEVER);
        HBox.setHgrow(this.passwordField, Priority.ALWAYS);
        VBox.setVgrow(passwordInput, Priority.NEVER);
        vBox.getChildren().add(passwordInput);

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
        Button cancel = new Button("Cancel");
        cancel.setPrefHeight(BUTTON_HEIGHT);
        cancel.setPadding(paddingH);
        Button create = new Button("Create Account");
        create.setPrefHeight(BUTTON_HEIGHT);
        create.setPadding(paddingH);
        JfxUtil.buttonPressCallback(cancel, stage::close);
        JfxUtil.buttonPressCallback(create, this::createAccount);

        HBox buttons = new HBox(cancel, spacing2, create);
        buttons.setPadding(paddingCore);
        HBox.setHgrow(spacing2, Priority.ALWAYS);
        HBox.setHgrow(cancel, Priority.NEVER);
        HBox.setHgrow(create, Priority.NEVER);
        VBox.setVgrow(buttons, Priority.NEVER);
        vBox.getChildren().add(buttons);

        Scene scene = new Scene(vBox);

        stage.setWidth(300);
        stage.setHeight(175);
        stage.setResizable(true);
        stage.setTitle("Create an Account");
        stage.setScene(scene);
    }

    private void createAccount() {
        Result<String, String> res = ParseUtil.validateUsername(this.username.getText());
        if (res.isError()) {
            this.failureReason.setText(INVALID_USERNAME.formatted(res.unwrapError()));
            return;
        }
        String username = res.unwrap();

        String password = this.passwordField.getText();
        Result<Void, String> result = ParseUtil.validatePassword(password);
        if (result.isError()) {
            this.failureReason.setText(INVALID_PASSWORD.formatted(result.unwrapError()));
            return;
        }

        SecretKey encode;
        try {
            encode = CryptoHelper.generateUserPassKey(password.toCharArray(), username);
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Failed to calculate password hash.");
            LOGGER.error("PBKDF2 key derivation failure", e);
            return;
        }
        Cipher aesCipher = CryptoHelper.createAesCipher();
        try {
            aesCipher.init(Cipher.ENCRYPT_MODE, encode);
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher.");
            LOGGER.error("AES cipher initialization failed", e);
            return;
        }

        LOGGER.debug("Generating RSA keypair");
        KeyPair keyPair = CryptoHelper.RSA_KEY_GENERATOR.generateKeyPair();
        LOGGER.debug("Keypair generation done (id: {})", CryptoHelper.sha256Hash(keyPair.getPublic().getEncoded()));
        try {
            this.client.config.addAccount(new Account(username, (RSAPublicKey) keyPair.getPublic(), new AccountData((RSAPrivateKey) keyPair.getPrivate(), new HashMap<>(), new HashMap<>()).encrypt(aesCipher)));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }

        this.stage.close();
    }
}
