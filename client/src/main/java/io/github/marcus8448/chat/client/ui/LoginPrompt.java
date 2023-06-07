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
import io.github.marcus8448.chat.client.network.AuthenticationData;
import io.github.marcus8448.chat.core.Result;
import io.github.marcus8448.chat.client.parse.ParseUtil;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.StageStyle;

import java.net.InetSocketAddress;
import java.util.Objects;

public class LoginPrompt {
    private static final String NO_SERVER = "Invalid server: %s";
    private static final String INVALID_USERNAME = "Invalid username: %s";
    private static final String INVALID_PASSWORD = "Invalid password: %s";
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
        Result<InetSocketAddress, String> result = ParseUtil.parseServerAddress(this.serverAddress.getText());
        if (result.isError()) {
            this.failureReason.setText(NO_SERVER.formatted(result.unwrapError()));
            return;
        }
        InetSocketAddress address = result.unwrap();

        Result<String, String> res = ParseUtil.validateUsername(this.username.getText());
        if (res.isError()) {
            this.failureReason.setText(INVALID_USERNAME.formatted(res.unwrapError()));
            return;
        }
        String username = res.unwrap();

        String password = this.passwordField.getText();
        Result<Void, String> res1 = ParseUtil.validatePassword(password);
        if (res1.isError()) {
            this.failureReason.setText(INVALID_PASSWORD.formatted(res1.unwrapError()));
            return;
        }

        if (mode == Mode.LOGIN) {
            Result<AuthenticationData, String> auth = ServerAuth.auth(address, username, password);
            if (!auth.isOk()) {
                this.failureReason.setText(auth.unwrapError());
            } else {
                AuthenticationData unwrap = auth.unwrap();
                System.out.println("SUCCESS");
            }
        } else {
            Result<String, String> response = ServerAuth.createAccount(address, username, password);
            if (!response.isOk()) {
                this.failureReason.setText(response.unwrapError());
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initStyle(StageStyle.UTILITY);
                alert.setTitle(response.unwrap());
                alert.setHeaderText("Account created");
                alert.setContentText("You may now login with your credentials.");
                alert.showAndWait();
                mode = Mode.SIGN_UP;
                this.changeMode(null);
            }
        }
    }

    public void passwordType(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            login();
        }
    }

    public void cancel(MouseEvent mouseEvent) {
        System.exit(0);
    }

    public void usernameTyped(KeyEvent keyEvent) {
        if (Objects.equals(failureReason.getText(), INVALID_USERNAME)) {
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
