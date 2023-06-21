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
import io.github.marcus8448.chat.core.api.misc.Identifier;
import io.github.marcus8448.chat.core.api.misc.Result;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Screen/UI for creating a new account
 */
public class CreateAccountScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String INVALID_USERNAME = "Invalid username: %s";
    private static final String INVALID_PASSWORD = "Invalid password: %s";

    /**
     * The client instance
     */
    private final Client client;
    /**
     * Password input field
     */
    private final PasswordField passwordField = new PasswordField();
    /**
     * Username input filed
     */
    private final TextField username = new TextField();
    /**
     * Displays the reason why an account could not be created
     */
    private final Label failureReason = new Label();
    /**
     * The current stage
     */
    private final Stage stage;

    public CreateAccountScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;
        VBox vBox = new VBox(); // root content pane
        JfxUtil.initVbox(vBox);

        double len = JfxUtil.getTextWidth("Username");
        vBox.getChildren().add(JfxUtil.createInputRow(new Label("Username"), this.username, "example", len));
        vBox.getChildren().add(JfxUtil.createInputRow(new Label("Password"), this.passwordField, "password", len));

        JfxUtil.setupFailureLabel(this.failureReason);
        vBox.getChildren().add(this.failureReason);

        vBox.getChildren().add(JfxUtil.createSpacing());

        Button cancel = new Button("Cancel"); // cancel button
        Button create = new Button("Create Account"); // create account button
        JfxUtil.buttonPressCallback(cancel, stage::close); // close the account creation window
        JfxUtil.buttonPressCallback(create, this::createAccount); // create the account

        JfxUtil.unescapedEnterCallback(this.passwordField, this::createAccount);

        vBox.getChildren().add(JfxUtil.createButtonRow(cancel, null, create));

        // create the scene, set up the stage
        Scene scene = new Scene(vBox);

        stage.setTitle("Create an Account");
        JfxUtil.resizeAutoHeight(stage, scene, 700.0);
    }

    /**
     * Try to create an account
     */
    private void createAccount() {
        // maybe i've been doing too much rust...
        Result<Identifier, String> res = Identifier.parse(this.username.getText()); // validate username
        if (res.isError()) { // check if invalid
            this.failureReason.setText(INVALID_USERNAME.formatted(res.unwrapError()));
            return; // cancel
        }
        Identifier username = res.unwrap();

        String password = this.passwordField.getText(); // get password
        Result<Void, String> result = ParseUtil.validatePassword(password); // validate password
        if (result.isError()) { // check if invalid
            this.failureReason.setText(INVALID_PASSWORD.formatted(result.unwrapError()));
            return; // cancel
        }

        SecretKey passKey; // AES key generated based on the username and password
        try {
            passKey = CryptoHelper.generateUserPassKey(password.toCharArray(), username.getValue());
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Failed to calculate password hash.");
            LOGGER.error("PBKDF2 key derivation failure", e);
            return;
        }

        Cipher aesCipher = CryptoHelper.createAesCipher();
        try {
            aesCipher.init(Cipher.ENCRYPT_MODE, passKey); // create a cipher to encrypt data
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher.");
            LOGGER.error("AES cipher initialization failed", e);
            return;
        }

        LOGGER.info("Generating RSA keypair");
        KeyPair keyPair = CryptoHelper.RSA_KEY_GENERATOR.generateKeyPair(); // Generate a RSA keypair (4096 bits)
        LOGGER.info("Keypair generation done (id: {})", CryptoHelper.sha256Hash(keyPair.getPublic().getEncoded()));
        try {
            // create and add the new account
            this.client.config.addAccount(new Account(username, (RSAPublicKey) keyPair.getPublic(), new AccountData((RSAPrivateKey) keyPair.getPrivate(), new HashMap<>(), new HashMap<>(), new ArrayList<>()).encrypt(aesCipher)));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            this.failureReason.setText("Failed to finalize account.");
            LOGGER.fatal("Account data encryption failed", e);
            return;
        }

        this.stage.close(); // close the window
    }
}
