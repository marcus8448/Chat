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
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.packet.ClientPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.ServerPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.client.Authenticate;
import io.github.marcus8448.chat.core.api.network.packet.client.Hello;
import io.github.marcus8448.chat.core.api.network.packet.server.AuthenticationRequest;
import io.github.marcus8448.chat.core.api.network.packet.server.AuthenticationSuccess;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public class LoginScreen {
    private static final Logger LOGGER = LogManager.getLogger();

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
        int lastAccount = this.client.config.getLastAccount();
        if (lastAccount >= 0 && lastAccount < this.client.config.getAccounts().size()) {
            this.accountBox.getSelectionModel().select(lastAccount);
        }
        this.accountBox.setConverter(JfxUtil.ACCOUNT_STRING_CONVERTER);
        VBox vBox = new VBox();
        JfxUtil.initializePadding(vBox);

        double len = JfxUtil.getTextWidth("Hostname");
        vBox.getChildren().add(this.createServerSelection(len));
        vBox.getChildren().add(JfxUtil.createComboInputRow(new Label("Account"), this.accountBox, len));
        vBox.getChildren().add(JfxUtil.createInputRow(new Label("Password"), this.passwordField, "password", len));

        JfxUtil.unescapedEnterCallback(this.passwordField, this::login);

        JfxUtil.setupFailureLabel(this.failureReason);
        vBox.getChildren().add(this.failureReason);

        vBox.getChildren().add(JfxUtil.createSpacing());

        Label createAccountPrompt = new Label("No account? Create one!");
        createAccountPrompt.setTextFill(JfxUtil.LINK_COLOUR);
        Button cancel = new Button("Cancel");
        Button login = new Button("Login");
        JfxUtil.buttonPressCallback(cancel, this.client::shutdown);
        JfxUtil.buttonPressCallback(login, this::login);
        JfxUtil.buttonPressCallback(createAccountPrompt, this::createAccount);
        vBox.getChildren().add(JfxUtil.createButtonRow(createAccountPrompt, null, cancel, login));

        MenuBar menuBar = createMenuBar();
        VBox.setVgrow(menuBar, Priority.NEVER);
        VBox.setVgrow(vBox, Priority.ALWAYS);
        VBox root = new VBox(menuBar, vBox);
        Scene scene = new Scene(root);

        stage.setResizable(true);
        stage.setScene(scene);
        JfxUtil.resizeAutoHeight(stage, scene, 700.0);
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
        String password = this.passwordField.getText();
        if (password.isBlank()) {
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
        SecretKey aesKey;
        try {
            aesKey = CryptoHelper.generateUserPassKey(password.toCharArray(), account.username().getValue());
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

        this.client.config.setLastAccount(this.accountBox.getSelectionModel().getSelectedIndex());
        AccountData accountData;
        try {
            accountData = account.data().decrypt(aesCipher);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
            this.failureReason.setText("Incorrect username/password");
            LOGGER.error("Account data decryption failed", e);
            return;
        }

        this.connectToServer(address, account, aesKey, publicKey, accountData);
    }

    private void connectToServer(InetSocketAddress address, Account account, SecretKey aesKey, RSAPublicKey publicKey, AccountData accountData) {
        PacketPipeline connect;
        try {
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect(address);
            connect = PacketPipeline.createNetwork(Constants.PACKET_HEADER, socket);
            connect.send(ClientPacketTypes.HELLO, new Hello(Constants.BRAND, Constants.VERSION, publicKey));

            Packet<AuthenticationRequest> packet = connect.receivePacket();
            RSAPublicKey serverKey = packet.data().getServerKey();
            String keyHash = CryptoHelper.sha256Hash(serverKey.getEncoded());
            LOGGER.info("Server key id: {}", keyHash);
            String host = address.getHostString() + ":" + address.getPort();
            RSAPublicKey expectedKey = accountData.knownServers().get(host);
            if (expectedKey == null) {
                LOGGER.info("Awaiting key confirmation");
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please verify that the server's key is correct\n\n" + keyHash + "\n\nDo you wish to connect?", ButtonType.NO, ButtonType.YES);
                alert.setTitle("Server ID Confirmation");
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) {
                    LOGGER.warn("Server connection aborted");
                    return;
                }
                LOGGER.warn("Trusting new server key");
                accountData.knownServers().put(host, serverKey);
            } else if (!expectedKey.equals(serverKey)) {
                LOGGER.warn("Server connection suspended - key change");
                String expectedHash = CryptoHelper.sha256Hash(serverKey.getEncoded());
                Alert alert = new Alert(Alert.AlertType.ERROR, "Server key ID changed!\nPrevious: " + expectedHash + "\nNew: " + keyHash + "\nPlease verify that the new key is correct. Do you wish to continue connecting?", ButtonType.NO, ButtonType.YES);
                alert.setTitle("Server ID Changed!");
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) {
                    LOGGER.warn("Server connection aborted");
                    return;
                }
                LOGGER.warn("Trusting new server key");
                accountData.knownServers().put(host, serverKey);
            }

            LOGGER.info("Initializing ciphers");
            Cipher cipher = CryptoHelper.createRsaCipher();
            cipher.init(Cipher.DECRYPT_MODE, accountData.privateKey());
            byte[] encodedKey = cipher.doFinal(packet.data().getAuthData());
            cipher.init(Cipher.ENCRYPT_MODE, serverKey);
            byte[] output = cipher.doFinal(encodedKey);

            LOGGER.info("Decoding symmetric (AES) key");
            SecretKey key;
            try {
                key = CryptoHelper.decodeAesKey(encodedKey);
            } catch (InvalidKeySpecException e) {
                throw new IllegalStateException(e);
            }

            LOGGER.info("Authenticating...");
            connect.send(ClientPacketTypes.AUTHENTICATE, new Authenticate(account.username(), output));
            Packet<?> response = connect.receivePacket();
            if (response.type() == ServerPacketTypes.AUTHENTICATION_SUCCESS) {
                LOGGER.info("Successfully authenticated to the server");
                AuthenticationSuccess success = response.getAs(ServerPacketTypes.AUTHENTICATION_SUCCESS);
                this.client.initialize(connect.encryptWith(key), aesKey, serverKey, publicKey, accountData, success.getUsers(), account.username(), address);
                this.client.saveAccountData();
                this.stage.close();
                ChatView chatView = new ChatView(this.client, this.stage);
                this.stage.show();
            } else if (response.type() == ServerPacketTypes.AUTHENTICATION_FAILURE) {
                String failure = response.getAs(ServerPacketTypes.AUTHENTICATION_FAILURE).getReason();
                LOGGER.error("Server denied connection: {}", failure);
                this.failureReason.setText(failure);
            } else {
                LOGGER.error("Server sent invalid id: " + response.type());
            }
        } catch (IOException e) {
            LOGGER.error("Communication I/O error", e);
            this.failureReason.setText("Failed to connect to server: " + e.getMessage());
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Generic connection crypto failure", e);
            this.failureReason.setText("Encryption failure: " + e.getMessage());
        }
    }

    private @NotNull HBox createServerSelection(double len) {
        HBox row = JfxUtil.createInputRow(new Label("Hostname"), this.hostname, "hostname", len);
        Label portLabel = new Label("Port");
        portLabel.setPadding(new Insets(0, 0, 0, JfxUtil.SPACING / 2.0));
        row.getChildren().add(portLabel);
        this.port.setPrefWidth(70);
        this.port.setMaxHeight(Integer.MAX_VALUE);
        row.getChildren().add(this.port);
        return row;
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

    private double alignLabel(Label label) {
        double hostname1 = JfxUtil.getTextWidth("Hostname");
        label.setPrefWidth(hostname1);
        return hostname1;
    }
}
