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
import io.github.marcus8448.chat.core.api.Constants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class LoginScreen {
    private static final int PADDING = 12;
    private static final int MAX = Integer.MAX_VALUE;

    protected static final Insets PADDING_HOR = new Insets(0, PADDING, 0, PADDING);
    protected static final Insets PADDING_CORE = new Insets(PADDING / 2.0, PADDING, PADDING / 2.0, PADDING / 2.0);

    private final Client client;

    private final ObservableList<Account> accounts;

    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<Account> accountBox;
    private final TextField hostname = new TextField("127.0.0.1");
    private final TextField port = new TextField(String.valueOf(Constants.PORT));

    public LoginScreen(Client client, Stage stage) {
        this.client = client;
        this.client.accounts.add(new Account("hello_there", new byte[0], new byte[] {23, 123, 59, 26, 100, 38, 76, 23, 98, 120}));
        this.accounts = FXCollections.observableArrayList(this.client.accounts);
        this.accountBox = new ComboBox<>(this.accounts);
        VBox vBox = new VBox();

        vBox.getChildren().add(this.createMenuBar());
        vBox.getChildren().add(this.createServerSelection());
        vBox.getChildren().add(this.createAccountSelection());
        vBox.getChildren().add(this.createPasswordInput());

        AnchorPane spacing = new AnchorPane();
        VBox.setVgrow(spacing, Priority.ALWAYS);
        vBox.getChildren().add(spacing);

        vBox.getChildren().add(createButtons());

        Scene scene = new Scene(vBox);

        stage.setWidth(400);
        stage.setHeight(230);
        stage.setResizable(true);
        stage.setScene(scene);
    }

    private void createAccount() {
    }

    private void importAccount() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import account");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Chat account", "*.account"));
        List<File> files = chooser.showOpenMultipleDialog(this.accountBox.getScene().getWindow());

    }

    private void exportAccount() {
        Stage stage = new Stage();
        ExportAccountScreen screen = new ExportAccountScreen(client, stage);
        stage.showAndWait();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export account");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Chat account", "*.account"));
        File file = chooser.showSaveDialog(this.accountBox.getScene().getWindow());
    }

    private void login() {
        System.out.println("LOGIN");
    }

    private @NotNull HBox createButtons() {
        Label createAccountPrompt = new Label("No account? Create one!");
        createAccountPrompt.setTextFill(Paint.valueOf("#21a7ff"));
        createAccountPrompt.setPadding(new Insets(0.0, 0.0, 0.0, PADDING / 2.0));
        JfxUtil.buttonPressCallback(createAccountPrompt, this::createAccount);
        AnchorPane spacing2 = new AnchorPane();
        Button cancel = new Button("Cancel");
        cancel.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        cancel.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        cancel.setPadding(PADDING_HOR);
        cancel.setOnMouseClicked(e -> Platform.exit());

        Button login = new Button("Login");
        login.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        login.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        login.setPadding(PADDING_HOR);
        JfxUtil.buttonPressCallback(login, this::login);

        HBox buttons = new HBox(createAccountPrompt, spacing2, cancel, login);
        buttons.setPadding(PADDING_CORE);
        HBox.setHgrow(createAccountPrompt, Priority.NEVER);
        HBox.setHgrow(spacing2, Priority.ALWAYS);
        HBox.setHgrow(cancel, Priority.NEVER);
        HBox.setHgrow(login, Priority.NEVER);
        VBox.setVgrow(buttons, Priority.NEVER);
        return buttons;
    }

    private @NotNull HBox createPasswordInput() {
        Label passwordLabel = new Label("Password ");
        passwordLabel.setPadding(PADDING_HOR);
        this.passwordField.setMinHeight(25);
        this.passwordField.setPadding(PADDING_HOR);

        HBox passwordInput = new HBox(passwordLabel, this.passwordField);
        passwordInput.setPadding(PADDING_CORE);
        HBox.setHgrow(passwordLabel, Priority.NEVER);
        HBox.setHgrow(this.passwordField, Priority.ALWAYS);
        VBox.setVgrow(passwordInput, Priority.NEVER);
        this.passwordField.setOnKeyPressed(JfxUtil.enterKeyCallback(this::login));
        return passwordInput;
    }

    private @NotNull HBox createAccountSelection() {
        Label accountLabel = new Label("Account    ");
        accountLabel.setPadding(PADDING_HOR);
        this.accountBox.setMaxWidth(MAX);
        this.accountBox.setPadding(PADDING_HOR);
//        this.accountBox.setCellFactory(AccountCell::new);
        this.accountBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Account object) {
                return object == null ? "" : object.username() + " [" + JfxUtil.keyId(object.publicKey()) + "]";
            }

            @Override
            public Account fromString(String string) {
                return null;
            }
        });

        HBox accountSelection = new HBox(accountLabel, this.accountBox);
        accountSelection.setPadding(PADDING_CORE);
        HBox.setHgrow(accountLabel, Priority.NEVER);
        HBox.setHgrow(this.accountBox, Priority.ALWAYS);
        VBox.setVgrow(accountSelection, Priority.NEVER);
        return accountSelection;
    }

    private @NotNull HBox createServerSelection() {
        Label hostnameLabel = new Label("Hostname");
        hostnameLabel.setPadding(PADDING_HOR);
        Label portLabel = new Label("Port");
        portLabel.setPadding(PADDING_HOR);
        this.port.setPrefWidth(70);
        this.port.setMaxHeight(MAX);
        this.hostname.setMinHeight(25);
        this.hostname.setMaxWidth(MAX);
        this.port.setPadding(PADDING_HOR);

        HBox serverSelection = new HBox(hostnameLabel, this.hostname, portLabel, this.port);
        serverSelection.setPadding(new Insets(PADDING, PADDING, PADDING / 2.0, PADDING / 2.0));
        HBox.setHgrow(hostnameLabel, Priority.NEVER);
        HBox.setHgrow(this.hostname, Priority.ALWAYS);
        HBox.setHgrow(portLabel, Priority.NEVER);
        HBox.setHgrow(this.port, Priority.NEVER);
        VBox.setVgrow(serverSelection, Priority.NEVER);
        return serverSelection;
    }

    private @NotNull MenuBar createMenuBar() {
        MenuItem create = new MenuItem("Create");
        create.setOnAction(e -> this.createAccount());
        MenuItem importAc = new MenuItem("Import");
        importAc.setOnAction(e -> this.importAccount());
        MenuItem export = new MenuItem("Export");
        export.setOnAction(e -> this.exportAccount());
        Menu account = new Menu("Account", null, create, importAc, export);
        MenuItem about = new MenuItem("About");

        Menu help = new Menu("Help", null, about);
        MenuBar menuBar = new MenuBar(account, help);
        VBox.setVgrow(menuBar, Priority.NEVER);
        return menuBar;
    }
}
