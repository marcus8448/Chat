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

    /**
     * The client instance
     */
    private final Client client;

    /**
     * The password input field
     */
    private final PasswordField passwordField = new PasswordField();
    /**
     * Account selection box
     */
    private final ComboBox<Account> accountBox;
    /**
     * The hostname of the server to connect to
     */
    private final TextField hostname = new TextField("127.0.0.1");
    /**
     * The port of the server to connect to
     */
    private final TextField port = new TextField(String.valueOf(Constants.PORT));
    /**
     * Displays the reason why the login failed
     */
    private final Label failureReason = new Label();
    /**
     * The primary stage
     */
    private final Stage stage;

    public LoginScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;
        this.stage.setTitle("Login");
        this.accountBox = new ComboBox<>(this.client.config.getAccounts());
        int lastAccount = this.client.config.getLastAccount();

        // select the last selected account (according to the config)
        if (lastAccount >= 0 && lastAccount < this.client.config.getAccounts().size()) {
            this.accountBox.getSelectionModel().select(lastAccount);
        }
        this.accountBox.setConverter(JfxUtil.ACCOUNT_STRING_CONVERTER);

        VBox vBox = new VBox();
        JfxUtil.initVbox(vBox);

        // create input fields
        double len = JfxUtil.getTextWidth("Hostname");
        vBox.getChildren().add(this.createServerSelection(len));
        vBox.getChildren().add(JfxUtil.createComboInputRow(new Label("Account"), this.accountBox, len));
        vBox.getChildren().add(JfxUtil.createInputRow(new Label("Password"), this.passwordField, "password", len));

        // when enter is pressed login
        JfxUtil.unescapedEnterCallback(this.passwordField, this::login);

        // add failure reason label
        JfxUtil.setupFailureLabel(this.failureReason);
        vBox.getChildren().add(this.failureReason);

        // put spacing between the input and buttons
        vBox.getChildren().add(JfxUtil.createSpacing());

        // create buttons
        Label createAccountPrompt = new Label("No account? Create one!");
        createAccountPrompt.setTextFill(JfxUtil.LINK_COLOUR);
        Button cancel = new Button("Cancel");
        Button login = new Button("Login");
        // set callbacks
        JfxUtil.buttonPressCallback(cancel, this.client::shutdown);
        JfxUtil.buttonPressCallback(login, this::login);
        JfxUtil.buttonPressCallback(createAccountPrompt, this::createAccount);
        vBox.getChildren().add(JfxUtil.createButtonRow(createAccountPrompt, null, cancel, login));

        // create top menu bar
        MenuBar menuBar = createMenuBar();
        VBox.setVgrow(menuBar, Priority.NEVER);
        VBox.setVgrow(vBox, Priority.ALWAYS);
        VBox root = new VBox(menuBar, vBox);
        Scene scene = new Scene(root);

        stage.setResizable(true);
        stage.setScene(scene);
        JfxUtil.resizeAutoHeight(stage, scene, 700.0);
    }

    /**
     * Opens the account creation window
     */
    private void createAccount() {
        LOGGER.info("Opening account creation screen");
        Stage stage = new Stage();
        CreateAccountScreen createAccountScreen = new CreateAccountScreen(this.client, stage);
        stage.showAndWait();
    }

    /**
     * Opens the account import window
     */
    private void importAccount() {
        LOGGER.info("Importing account");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import account");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Chat account", "*.account"));
        // prompt the user to select files to open
        List<File> files = chooser.showOpenMultipleDialog(this.accountBox.getScene().getWindow());
        if (files == null) {
            LOGGER.debug("No files selected for import");
            return;
        }

        // add the accounts
        for (File file : files) {
            LOGGER.debug("Importing account from file: {}", file);
            try (FileReader reader = new FileReader(file)) { // read the file
                Account account = Config.GSON.fromJson(reader, Account.class); //parse the account
                this.client.config.addAccount(account); // add the account
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

    /**
     * Open the account export screen
     */
    private void exportAccount() {
        LOGGER.info("Opening account export screen");
        Stage stage = new Stage();
        ExportAccountScreen screen = new ExportAccountScreen(client, stage);
        stage.showAndWait();
    }

    /**
     * Open the account edit screen
     */
    private void editAccount() {
        LOGGER.info("Opening account edit screen");
        Stage stage = new Stage();
        EditAccountScreen screen = new EditAccountScreen(client, stage);
        stage.showAndWait();
    }

    /**
     * Login to the server
     */
    private void login() {
        LOGGER.info("Attempting to login");

        // check that all input is valid

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

        // create the config decryption key
        SecretKey passKey;
        try {
            passKey = CryptoHelper.generateUserPassKey(password.toCharArray(), account.username().getValue());
        } catch (InvalidKeySpecException e) {
            this.failureReason.setText("Invalid account/password");
            LOGGER.error("Failed to generate AES secret", e);
            return;
        }

        RSAPublicKey publicKey = account.publicKey();
        Cipher aesCipher = CryptoHelper.createAesCipher();
        try {
            aesCipher.init(Cipher.DECRYPT_MODE, passKey);
        } catch (InvalidKeyException e) {
            this.failureReason.setText("Failed to initialize AES cipher");
            LOGGER.error("Failed to initialize AES cipher with key", e);
            return;
        }

        //set the last used account to this one
        this.client.config.setLastAccount(this.accountBox.getSelectionModel().getSelectedIndex());
        // decrypt account data
        AccountData accountData;
        try {
            accountData = account.data().decrypt(aesCipher);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
            this.failureReason.setText("Incorrect username/password");
            LOGGER.error("Account data decryption failed", e);
            return;
        }

        //connect to the server.
        this.connectToServer(address, account, passKey, publicKey, accountData);
    }

    /**
     * Connects to the server at the given address with the given account details
     *
     * @param address     the server's address
     * @param account     the account to use
     * @param aesKey      the key to use for config encryption/decryption
     * @param publicKey   the user's public key
     * @param accountData the data associated with the account
     */
    private void connectToServer(InetSocketAddress address, Account account, SecretKey aesKey, RSAPublicKey publicKey, AccountData accountData) {
        PacketPipeline connect;
        try {
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect(address); // connect to the address

            // create a pipeline for the connection
            connect = PacketPipeline.createNetwork(Constants.PACKET_HEADER, socket);
            // send initial packet
            connect.send(ClientPacketTypes.HELLO, new Hello(Constants.BRAND, Constants.VERSION, publicKey));

            // get the server's response
            Packet<AuthenticationRequest> packet = connect.receivePacket();

            RSAPublicKey serverKey = packet.data().getServerKey();

            // manage server keys
            String keyHash = CryptoHelper.sha256Hash(serverKey.getEncoded());
            LOGGER.info("Server key id: {}", keyHash);
            String host = address.getHostString() + ":" + address.getPort();

            // verify that the server is the correct one
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

            //setup encryption for server connection
            LOGGER.info("Initializing ciphers");
            Cipher cipher = CryptoHelper.createRsaCipher();
            cipher.init(Cipher.DECRYPT_MODE, accountData.privateKey());
            byte[] encodedKey = cipher.doFinal(packet.data().getAuthData());
            cipher.init(Cipher.ENCRYPT_MODE, serverKey);
            byte[] output = cipher.doFinal(encodedKey);

            // get the server connection session key
            LOGGER.info("Decoding session key");
            SecretKey sessionKey;
            try {
                sessionKey = CryptoHelper.decodeAesKey(encodedKey);
            } catch (InvalidKeySpecException e) {
                throw new IllegalStateException(e);
            }

            LOGGER.info("Authenticating...");
            // send the client's data to the server
            connect.send(ClientPacketTypes.AUTHENTICATE, new Authenticate(account.username(), output));

            Packet<?> response = connect.receivePacket();
            // check the server's response
            if (response.type() == ServerPacketTypes.AUTHENTICATION_SUCCESS) {
                LOGGER.info("Successfully authenticated to the server");
                AuthenticationSuccess success = response.getAs(ServerPacketTypes.AUTHENTICATION_SUCCESS);
                // initialize the client with the new account data and connection info
                this.client.initialize(connect.encryptWith(sessionKey), aesKey, serverKey, publicKey, accountData, success.getUsers(), account.username(), address);
                this.client.saveAccountData();
                this.stage.close();
                // OPEN the main window
                ChatView chatView = new ChatView(this.client, this.stage);
                this.stage.show();
            } else if (response.type() == ServerPacketTypes.AUTHENTICATION_FAILURE) {
                //the connection failed so tell the user why
                String failure = response.getAs(ServerPacketTypes.AUTHENTICATION_FAILURE).getReason();
                LOGGER.error("Server denied connection: {}", failure);
                this.failureReason.setText(failure);
            } else {
                LOGGER.error("Server sent invalid id: " + response.type());
                this.failureReason.setText("Invalid server response");
            }
        } catch (IOException e) {
            LOGGER.error("Communication I/O error", e);
            this.failureReason.setText("Failed to connect to server: " + e.getMessage());
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Generic connection crypto failure", e);
            this.failureReason.setText("Encryption failure: " + e.getMessage());
        }
    }

    /**
     * Creates a row of input for the server address and port
     *
     * @param len the alignment for the "Hostname" label
     * @return the new row of input boxes
     */
    private @NotNull HBox createServerSelection(double len) {
        // Hostname [input]  Port [input]
        HBox row = JfxUtil.createInputRow(new Label("Hostname"), this.hostname, "hostname", len);
        Label portLabel = new Label("Port");
        portLabel.setPadding(new Insets(0, 0, 0, JfxUtil.SPACING / 2.0));
        row.getChildren().add(portLabel);
        this.port.setPrefWidth(70);
        this.port.setMaxHeight(Integer.MAX_VALUE);
        row.getChildren().add(this.port);
        return row;
    }

    /**
     * Creates the menu bar at the top of the screen
     *
     * @return the menu bar
     */
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

        MenuBar menuBar = new MenuBar(file, account);
        VBox.setVgrow(menuBar, Priority.NEVER);
        return menuBar;
    }
}
