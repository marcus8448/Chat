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
import io.github.marcus8448.chat.client.config.Config;
import io.github.marcus8448.chat.client.util.JfxUtil;
import io.github.marcus8448.chat.core.api.Constants;
import io.github.marcus8448.chat.core.api.network.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.network.connection.BinaryOutput;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.network.PacketTypes;
import io.github.marcus8448.chat.core.network.packet.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public class LoginScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int PADDING = 12;
    private static final int MAX = Integer.MAX_VALUE;

    protected static final Insets PADDING_HOR = new Insets(0, PADDING, 0, PADDING);
    protected static final Insets PADDING_CORE = new Insets(PADDING / 2.0, PADDING, PADDING / 2.0, PADDING / 2.0);

    private final Client client;

    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<Account> accountBox;
    private final TextField hostname = new TextField("127.0.0.1");
    private final TextField port = new TextField(String.valueOf(Constants.PORT));
    private final Label failureReason = new Label();
    private final Stage stage;

    public LoginScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;
        this.stage.setTitle("Login");
        this.accountBox = new ComboBox<>(this.client.config.getAccounts());
        VBox vBox = new VBox();

        vBox.getChildren().add(this.createMenuBar());
        vBox.getChildren().add(this.createServerSelection());
        vBox.getChildren().add(this.createAccountSelection());
        vBox.getChildren().add(this.createPasswordInput());

        this.failureReason.setPrefWidth(MAX);
        this.failureReason.setAlignment(Pos.TOP_RIGHT);
        HBox spacing = new HBox(this.failureReason);
        HBox.setHgrow(this.failureReason, Priority.ALWAYS);
        spacing.setPadding(PADDING_HOR);
        VBox.setVgrow(spacing, Priority.ALWAYS);
        vBox.getChildren().add(spacing);

        vBox.getChildren().add(createButtons());

        Scene scene = new Scene(vBox);

        stage.setWidth(400);
        stage.setHeight(250);
        stage.setResizable(true);
        stage.setScene(scene);
    }

    private void createAccount() {
        LOGGER.info("Opening account creation screen");
        Stage stage = new Stage();
        CreateAccountScreen createAccountScreen = new CreateAccountScreen(this.client, stage);
        stage.showAndWait();
    }

    private void importAccount() {
        LOGGER.info("Importing account");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import account");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Chat account", "*.account"));
        List<File> files = chooser.showOpenMultipleDialog(this.accountBox.getScene().getWindow());
        if (files == null) {
            LOGGER.debug("No files selected for import");
            return;
        }

        for (File file : files) {
            LOGGER.debug("Importing account from file: {}", file);
            try (FileReader reader = new FileReader(file)) {
                Account account = Config.GSON.fromJson(reader, Account.class);
                this.client.config.addAccount(account);
            } catch (IOException e) {
                LOGGER.error("Failed to read account file", e);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to read account file!");
                alert.showAndWait();
            } catch (Exception e) {
                LOGGER.error("Failed to parse account file", e);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to parse account file!");
                alert.showAndWait();
            }
        }
    }

    private void exportAccount() {
        LOGGER.info("Opening account export screen");
        Stage stage = new Stage();
        ExportAccountScreen screen = new ExportAccountScreen(client, stage);
        stage.showAndWait();
    }

    private void editAccount() {
        LOGGER.info("Opening account edit screen");
        Stage stage = new Stage();
        EditAccountScreen screen = new EditAccountScreen(client, stage);
        stage.showAndWait();
    }

    private void login() {
        LOGGER.info("Attempting to login");
        if (this.accountBox.getSelectionModel().isEmpty()) {
            this.failureReason.setText("You must select an account");
            return;
        }
        if (this.passwordField.getText().isBlank()) {
            this.failureReason.setText("You must enter a password.");
            return;
        }
        String hostname = this.hostname.getText();
        if (hostname.isBlank()) {
            this.failureReason.setText("Invalid hostname");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(this.port.getText());
            if (port <= 0 || port > 0xFFFF) {
                this.failureReason.setText("Invalid port");
                return;
            }
        } catch (NumberFormatException ignored) {
            this.failureReason.setText("Invalid port");
            return;
        }
        InetSocketAddress address = new InetSocketAddress(hostname, port);
        Account account = this.accountBox.getSelectionModel().getSelectedItem();

        LOGGER.debug("All required fields supplied");
        SecretKeySpec aesKey;
        try {
            aesKey = new SecretKeySpec(CryptoHelper.PBKDF2_SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(this.passwordField.getText().toCharArray(), account.username().getBytes(StandardCharsets.UTF_8), 65536, 256)).getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Invalid account/password");
            LOGGER.error("Failed to generate AES secret", e);
            return;
        }

        RSAPublicKey publicKey = account.publicKey();
        Cipher aesCipher = CryptoHelper.createAesCipher();
        try {
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher");
            LOGGER.error("Failed to initialize AES cipher with key", e);
            return;
        }
        AccountData accountData;
        try {
            accountData = account.data().decrypt(aesCipher);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
            this.failureReason.setText("Incorrect username/password");
            LOGGER.error("Account data decryption failed", e);
            return;
        }

        PacketPipeline connect;
        try {
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect(address);
            BinaryInput stream = BinaryInput.stream(socket.getInputStream());
            BinaryOutput output1 = BinaryOutput.stream(socket.getOutputStream());
            connect = PacketPipeline.createNetwork(Constants.PACKET_HEADER, socket);
            connect.send(PacketTypes.CLIENT_HELLO, new ClientHello(Constants.BRAND, Constants.VERSION, publicKey));

            Packet<ServerAuthRequest> packet = connect.receivePacket();
            Cipher cipher = CryptoHelper.createRsaCipher();
            cipher.init(Cipher.DECRYPT_MODE, accountData.privateKey());
            byte[] b = cipher.doFinal(packet.data().getAuthData());
            RSAPublicKey serverKey = packet.data().getServerKey();
            cipher.init(Cipher.ENCRYPT_MODE, serverKey);
            byte[] output = cipher.doFinal(b);

            connect.send(PacketTypes.CLIENT_AUTH, new ClientAuthResponse(account.username(), output));
            Packet<ServerAuthResponse> networkedDataPacket = connect.receivePacket();
            if (networkedDataPacket.data().isSuccess()) {
                this.client.setIdentity(connect.encryptWith(serverKey, accountData.privateKey()), serverKey, publicKey, accountData);
                this.stage.close();
                ChatView chatView = new ChatView(this.client, this.stage);
                this.stage.show();
            } else {
                LOGGER.error("Server denied connection: {}", networkedDataPacket.data().getFailureReason());
                this.failureReason.setText(networkedDataPacket.data().getFailureReason());
            }
        } catch (IOException e) {
            LOGGER.error("Communication I/O error", e);
            this.failureReason.setText("Failed to connect to server: " + e.getMessage());
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Generic connection crypto failure", e);
            this.failureReason.setText("Encryption failure: " + e.getMessage());
        }
    }

    private @NotNull HBox createButtons() {
        Label createAccountPrompt = new Label("No account? Create one!");
        createAccountPrompt.setTextFill(JfxUtil.LINK_COLOUR);
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
        Label accountLabel = new Label("Account");
        accountLabel.setPadding(PADDING_HOR);
        accountLabel.setMinWidth(77);
        this.accountBox.setMaxWidth(MAX);
        this.accountBox.setPadding(PADDING_HOR);
        this.accountBox.setConverter(JfxUtil.ACCOUNT_STRING_CONVERTER);

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
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(e -> this.editAccount());
        MenuItem importAc = new MenuItem("Import");
        importAc.setOnAction(e -> this.importAccount());
        MenuItem export = new MenuItem("Export");
        export.setOnAction(e -> this.exportAccount());
        Menu file = new Menu("File", null, importAc, export);
        Menu account = new Menu("Account", null, create, edit);
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> this.about());

        Menu help = new Menu("Help", null, about);
        MenuBar menuBar = new MenuBar(file, account, help);
        VBox.setVgrow(menuBar, Priority.NEVER);
        return menuBar;
    }

    private void about() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Chat");
        alert.showAndWait();
    }
}
