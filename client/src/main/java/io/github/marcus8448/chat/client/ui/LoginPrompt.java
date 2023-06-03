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

import io.github.marcus8448.chat.client.ServerAuth;
import io.github.marcus8448.chat.core.Constants;
import io.github.marcus8448.chat.core.network.packet.ServerAuthResponse;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;

public class LoginPrompt {
    private static final String NO_SERVER = "You must enter a server address";
    private static final String NO_USERNAME = "You must enter a username";
    private static final String NO_PASSWORD = "You must enter a password";
    private static final String NO_CREDENTIALS = "You must enter a username and password";
    private static final String NO_ACCOUNT = "No account? Sign up!";
    private static final String HAVE_ACCOUNT = "Have an account? Log in";
    private static final String SIGN_UP = "Sign up";
    private static final String LOGIN = "Login";
    public TextField serverAddress;

    private Mode mode = Mode.LOGIN;
    public PasswordField passwordField;
    public Button loginButton;
    public Button cancelButton;
    public TextField username;
    public Label failureReason;
    public Label modeSwapText;

    public void login() {
        if (this.username.getText().isBlank()) {
            if (this.passwordField.getText().isBlank()) {
                this.failureReason.setText(NO_CREDENTIALS);
            } else {
                this.failureReason.setText(NO_USERNAME);
            }
        } else if (this.passwordField.getText().isBlank()) {
            this.failureReason.setText(NO_PASSWORD);
        }

        String text = this.serverAddress.getText();
        int port = Constants.PORT;
        String[] split = text.split(":");
        if (split.length == 2) {
            port = Integer.parseInt(split[split.length - 1]);
            text = split[0];
        }

        if (mode == Mode.LOGIN) {
            ServerAuth.auth(InetSocketAddress.createUnresolved(text, port), this.username.getText(), this.passwordField.getText());
        } else {
            ServerAuthResponse response = ServerAuth.createAccount(new InetSocketAddress(text, port), this.username.getText(), this.passwordField.getText());
            JOptionPane.showMessageDialog(null, "Server id: " + response.getServerKey().toString(), "Server id", JOptionPane.INFORMATION_MESSAGE, null);
            if (!response.isSuccess()) {
                this.failureReason.setText(response.getFailureReason());
            } else {
                mode = Mode.SIGN_UP;
                this.changeMode(null);
            }
        }
    }

    public void passwordType(KeyEvent keyEvent) {
        if (Objects.equals(failureReason.getText(), NO_PASSWORD)) {
            failureReason.setText("");
        }
        if (keyEvent.getCode() == KeyCode.ENTER) {
            login();
        }
    }

    public void cancel(MouseEvent mouseEvent) {
        System.exit(0);
    }

    public void usernameTyped(KeyEvent keyEvent) {
        if (Objects.equals(failureReason.getText(), NO_USERNAME)) {
            failureReason.setText("");
        }
    }

    public void changeMode(MouseEvent mouseEvent) {
        this.failureReason.setText("");
        if (this.mode == Mode.LOGIN) {
            this.mode = Mode.SIGN_UP;
            this.loginButton.setText(SIGN_UP);
            this.modeSwapText.setText(HAVE_ACCOUNT);
        } else {
            this.mode = Mode.LOGIN;
            this.loginButton.setText(LOGIN);
            this.modeSwapText.setText(NO_ACCOUNT);
        }
    }

    enum Mode {
        LOGIN,
        SIGN_UP
    }
}
