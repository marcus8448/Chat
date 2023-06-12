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
import io.github.marcus8448.chat.core.api.connection.PacketPipeline;
import io.github.marcus8448.chat.core.api.crypto.CryptoConstants;
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
import javafx.scene.paint.Paint;
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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
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
            try {
                String s = Files.readString(file.toPath());
                if (s.startsWith("chat-account/")) {
                    String[] split = s.substring(13).split("\n");
                    if (split.length >= 3) {
                        try {
                            String username = split[0];
                            byte[] privateKey = Base64.getDecoder().decode(split[1]);
                            byte[] publicKey = Base64.getDecoder().decode(split[2]);
                            PublicKey publicKey1 = null;
                            try {
                                publicKey1 = CryptoConstants.RSA_KEY_FACTORY.generatePublic(new X509EncodedKeySpec(publicKey));
                            } catch (InvalidKeySpecException e) {
                                LOGGER.error("Failed to deserialize public key", e);
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid account key!");
                                alert.showAndWait();
                                continue;
                            }

                            Account account = new Account(username, privateKey, (RSAPublicKey) publicKey1);
                            this.client.config.addAccount(account);
                        } catch (IllegalArgumentException e) {
                            LOGGER.error("Invalid file encoding", e);
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to deserialize account!");
                            alert.showAndWait();
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read account file", e);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to read account file!");
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
            aesKey = new SecretKeySpec(CryptoConstants.PBKDF2_SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(this.passwordField.getText().toCharArray(), account.username().getBytes(StandardCharsets.UTF_8), 65536, 256)).getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Invalid account/password");
            LOGGER.error("Failed to generate AES secret", e);
            return;
        }

        RSAPublicKey publicKey = account.publicKey();
        Cipher aesCipher = CryptoConstants.getAesCipher();
        try {
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher");
            LOGGER.error("Failed to initialize AES cipher with key", e);
            return;
        }
        byte[] bytes;
        try {
            bytes = aesCipher.doFinal(account.privateKey());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            this.failureReason.setText("Incorrect username/password");
            LOGGER.error("Private key decryption failed", e);
            return;
        }
        RSAPrivateKey privateKey;
        try {
            privateKey = (RSAPrivateKey) CryptoConstants.RSA_KEY_FACTORY.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Incorrect username/password");
            LOGGER.error("Private key derivation failed", e);
            return;
        }

        PacketPipeline connect = null;
        try {
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect(address);
            connect = PacketPipeline.createNetworked(Constants.PACKET_HEADER, socket);
            connect.send(PacketTypes.CLIENT_HELLO, new ClientHello(Constants.VERSION, Constants.VERSION, publicKey));

            Packet<ServerAuthRequest> packet = connect.receivePacket();
            Cipher cipher = CryptoConstants.getRsaCipher();
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] b = cipher.doFinal(packet.data().getAuthData());
            RSAPublicKey serverKey = packet.data().getServerKey();
            cipher.init(Cipher.ENCRYPT_MODE, serverKey);
            byte[] output = cipher.doFinal(b);

            connect.send(PacketTypes.CLIENT_AUTH, new ClientAuthResponse(account.username(), output));
            Packet<ServerAuthResponse> networkedDataPacket = connect.receivePacket();
            if (networkedDataPacket.data().isSuccess()) {
                this.client.setIdentity(connect, serverKey, privateKey, publicKey);
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
