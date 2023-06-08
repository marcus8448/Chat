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
import io.github.marcus8448.chat.core.api.crypto.CryptoConstants;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

public class CreateAccountScreen {
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
        this.failureReason.setTextFill(Paint.valueOf("#ee1100"));
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
        System.out.println("generating key");
        KeyPair keyPair = CryptoConstants.RSA_KEY_GENERATOR.generateKeyPair();
        System.out.println("gen done");

        SecretKey encode = null;
        try {
            encode = new SecretKeySpec(CryptoConstants.PBKDF2_SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(password.toCharArray(), username.getBytes(StandardCharsets.UTF_8), 65536, 256)).getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Failed to calculate password hash.");
            e.printStackTrace();
            return;
        }
        Cipher aesCipher = CryptoConstants.getAesCipher();
        try {
            aesCipher.init(Cipher.ENCRYPT_MODE, encode);
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher.");
            e.printStackTrace();
            return;
        }
        byte[] bytes = null;
        try {
            bytes = aesCipher.doFinal(keyPair.getPrivate().getEncoded());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            this.failureReason.setText("Failed encrypt private key.");
            e.printStackTrace();
            return;
        }
        this.client.config.addAccount(new Account(username, bytes, (RSAPublicKey) keyPair.getPublic()));

        this.stage.close();
    }
}
