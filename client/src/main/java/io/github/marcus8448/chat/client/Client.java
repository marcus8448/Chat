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

package io.github.marcus8448.chat.client;

import io.github.marcus8448.chat.client.config.AccountData;
import io.github.marcus8448.chat.client.config.Config;
import io.github.marcus8448.chat.client.ui.ChatView;
import io.github.marcus8448.chat.client.ui.LoginScreen;
import io.github.marcus8448.chat.client.ui.UserTrustScreen;
import io.github.marcus8448.chat.core.api.Constants;
import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.message.*;
import io.github.marcus8448.chat.core.api.misc.Identifier;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.packet.ClientPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.ServerPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.client.Authenticate;
import io.github.marcus8448.chat.core.api.network.packet.client.Hello;
import io.github.marcus8448.chat.core.api.network.packet.common.ChannelList;
import io.github.marcus8448.chat.core.api.network.packet.server.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client extends Application implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * Map of channel id -> list of messages
     */
    public final Map<Identifier, ObservableList<Message>> messages = new HashMap<>();
    /**
     * Users connected to the server
     * Map of user session id -> user
     */
    public final ObservableMap<Integer, User> users = FXCollections.observableHashMap();
    /**
     * List of users connected to the same server.
     * Essentially a copy of users (but a list)
     *
     * @see Client#users
     */
    public final ObservableList<User> userList = FXCollections.observableArrayList();
    /**
     * AES cipher to encrypt client/server communications with
     */
    private final Cipher aesCipher = CryptoHelper.createAesCipher();
    /**
     * RSA signature for the signing of messages
     */
    private final Signature rsaSignature = CryptoHelper.createRsaSignature();
    /**
     * Whether the client should shutdown or attempt to reconnect to the server
     */
    public volatile boolean closeConnection = false;
    /**
     * The configuration data/file
     */
    public Config config;
    /**
     * The connection to the server. Might be null if one is not active
     */
    public PacketPipeline connection;
    /**
     * A list of channels that have been connected to
     */
    public ObservableList<Identifier> channels = FXCollections.observableArrayList();
    /**
     * The main screen/UI window
     */
    private ChatView screen;
    /**
     * The AES secret key derived from the username/password
     * Used for encrypting configuration data
     */
    private SecretKey passKey;
    /**
     * The public key of this client's account
     */
    private RSAPublicKey publicKey;
    /**
     * The public key of the server this client is connected to
     */
    private RSAPublicKey serverPubKey;
    /**
     * The active account's data
     */
    private AccountData accountData;
    /**
     * The primary stage/window
     */
    private Stage primaryStage;
    /**
     * Handle to the system tray, if available
     */
    private SystemTray systemTray;
    /**
     * Chat tray icon, if AWT is available
     */
    private TrayIcon trayIcon;
    /**
     * The address of the connected server
     */
    private InetSocketAddress address;
    /**
     * The username of the active account
     */
    private Identifier username;

    public Client() {
        this.users.addListener((MapChangeListener<Integer, User>) change -> { // sync list with map
            if (change.wasAdded()) {
                Platform.runLater(() -> this.userList.add(change.getValueAdded()));
            } else {
                Platform.runLater(() -> this.userList.remove(change.getValueRemoved()));
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        Parameters parameters = this.getParameters();
        String configName = parameters.getNamed().getOrDefault("config", "chat.json"); //get config file name
        this.config = Config.load(new File(configName)); // laod the config file
        this.primaryStage = primaryStage; // set the stage
        this.beginLoginProcess(primaryStage);
    }

    /**
     * Opens the initial login window
     *
     * @param primaryStage the stage to open the window on
     */
    private void beginLoginProcess(Stage primaryStage) {
        LoginScreen loginScreen = new LoginScreen(this, primaryStage); // create the screen

        // try to load the app icon
        try (InputStream iconStream = Client.class.getClassLoader().getResourceAsStream("icon.png")) {
            if (iconStream != null) { // check if it exists
                primaryStage.getIcons().add(new Image(iconStream)); // create the image
            } else {
                LOGGER.error("Missing icon!");
            }
        } catch (Exception e) {
            // missing icon is not fatal.
            LOGGER.warn("Failed to load icon image", e);
        }

        primaryStage.show(); // show the stage
    }

    /**
     * Logs out the current account, and re-opens the login screen
     *
     * @param stage the stage to open the screen on
     */
    public void logout(Stage stage) {
        this.closeConnection = true; // inform connection thread to stop
        try {
            this.connection.close(); // close server connection
        } catch (IOException ignored) {
        }

        // remove all channels, messages and other data
        this.channels.clear();
        this.connection = null;
        this.passKey = null;
        this.serverPubKey = null;
        this.accountData = null;
        this.messages.clear();
        this.address = null;
        this.users.clear();
        this.username = null;

        // remove the system tray icon
        if (this.trayIcon != null && this.systemTray != null) {
            this.systemTray.remove(this.trayIcon);
            this.trayIcon = null;
            this.systemTray = null;
        }

        // close the window, if open
        stage.hide();
        // open the login screen
        this.beginLoginProcess(stage);
    }

    /**
     * Initializes the client with a new server connection and account details.
     *
     * @param connection the connection to the server
     * @param passKey    the AES key that encrypts the account data
     * @param serverKey  the connected server's public RSA key
     * @param publicKey  the active account's public RSA key
     * @param data       the active account's decrypted data
     * @param users      the users online on the server
     * @param username   the username of the active account
     * @param address    the address of the connected server
     */
    public void initialize(PacketPipeline connection, SecretKey passKey, RSAPublicKey serverKey, RSAPublicKey publicKey, @NotNull AccountData data, List<User> users, Identifier username, InetSocketAddress address) {
        // store the state data
        this.connection = connection;
        this.closeConnection = false;
        this.address = address;
        this.passKey = passKey;
        this.publicKey = publicKey;
        this.serverPubKey = serverKey;
        this.accountData = data;
        this.username = username;
        // add default channel
        this.messages.putIfAbsent(Constants.BASE_CHANNEL, FXCollections.observableArrayList());
        if (!this.channels.contains(Constants.BASE_CHANNEL)) this.channels.add(0, Constants.BASE_CHANNEL);
        // add the connected users
        for (User user : users) {
            this.users.put(user.sessionId(), user);
        }
        // initialize signing key
        RSAPrivateKey privateKey = this.accountData.privateKey();
        try {
            this.rsaSignature.initSign(privateKey);
        } catch (InvalidKeyException e) {
            LOGGER.fatal("Failed to initialize signature", e);
            this.shutdown();
            return;
        }
        // open server -> client packet connection management thread
        Thread thread = new Thread(this, "Client Main");
        thread.start();

        // join relevant channels
        try {
            this.connection.send(ClientPacketTypes.JOIN_CHANNELS, new ChannelList(this.accountData.channels().stream().map(Identifier::create).toArray(Identifier[]::new)));
        } catch (Exception e) {
            LOGGER.fatal("Failed to request channels", e);
            this.shutdown();
            return;
        }

        // create the system tray icon if possible
        if (this.trayIcon == null) {
            // load the icon
            if (SystemTray.isSupported()) { // check if we can use the system tray
                try (InputStream resourceAsStream = Client.class.getClassLoader().getResourceAsStream("icon.png")) {
                    if (resourceAsStream != null) { // check that the icon exists exists
                        this.systemTray = SystemTray.getSystemTray(); // get the system tray
                        this.trayIcon = new TrayIcon(ImageIO.read(resourceAsStream)); // create the tray icon
                        MenuItem exit = new MenuItem("Exit");
                        exit.addActionListener(l -> Platform.runLater(this::shutdown));
                        PopupMenu popup = new PopupMenu("Chat v" + Constants.VERSION);
                        popup.add(exit);
                        // setup icon details
                        this.trayIcon.setToolTip("Chat v" + Constants.VERSION);
                        this.trayIcon.setPopupMenu(popup);
                        this.trayIcon.setImageAutoSize(true);
                        this.trayIcon.addActionListener(l -> {
                            if (l.getModifiers() == 0  // notification click on windows
                                    || l.getModifiers() == InputEvent.BUTTON1_DOWN_MASK // simple left click
                            ) {
                                Platform.runLater(() -> {
                                    // focus the window if not already active
                                    if (!this.primaryStage.isFocused()) {
                                        this.primaryStage.requestFocus();
                                    }
                                });
                            }
                        });
                        // add the icon to the tray
                        this.systemTray.add(this.trayIcon);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to load system tray");
                }
            }
        }
    }

    /**
     * Sign the given message contents with the active account's key
     *
     * @param contents the contents of the message
     * @return the signature
     */
    public byte[] signMessage(String contents) {
        try {
            this.rsaSignature.update(contents.getBytes(StandardCharsets.UTF_8));
            return this.rsaSignature.sign();
        } catch (SignatureException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Manages the client's connection with the server
     */
    @Override
    public void run() {
        try {
            while (this.connection.isOpen()) {
                Packet<?> packet = this.connection.receivePacket(); // get a packet from the server

                // act on the packet based on the type
                if (packet.type() == ServerPacketTypes.ADD_MESSAGE) {
                    // add a text message to a channel
                    AddMessage addMessage = packet.getAs(ServerPacketTypes.ADD_MESSAGE);
                    Platform.runLater(() -> {
                        TextMessage text = Message.text(addMessage.getTimestamp(), this.users.get(addMessage.getAuthorId()), addMessage.getContents(), addMessage.getSignature());
                        addMessage(addMessage.getChannel(), text);
                    });
                } else if (packet.type() == ServerPacketTypes.USER_CONNECT) {
                    // add a user to the online user list
                    User user = packet.getAs(ServerPacketTypes.USER_CONNECT).getUser();
                    Platform.runLater(() -> this.users.put(user.sessionId(), user));
                } else if (packet.type() == ServerPacketTypes.USER_DISCONNECT) {
                    // remove a user from the online user list
                    int id = packet.getAs(ServerPacketTypes.USER_DISCONNECT).getId();
                    Platform.runLater(() -> this.users.remove(id));
                } else if (packet.type() == ServerPacketTypes.SYSTEM_MESSAGE) {
                    // add a message from the SYSTEM user to the channel
                    SystemMessage systemMessage = packet.getAs(ServerPacketTypes.SYSTEM_MESSAGE);
                    Platform.runLater(() -> {
                        TextMessage text = Message.text(systemMessage.getTimestamp(), MessageAuthor.system(this.serverPubKey), systemMessage.getContents(), systemMessage.getSignature());
                        addMessage(systemMessage.getChannel(), text);
                    });
                } else if (packet.type() == ServerPacketTypes.ADD_CHANNELS) {
                    // add a channel to the list of connected channels
                    ChannelList list = packet.getAs(ServerPacketTypes.ADD_CHANNELS);
                    Platform.runLater(() -> {
                        for (Identifier listChannel : list.getChannels()) {
                            if (!this.channels.contains(listChannel)) {
                                this.channels.add(listChannel); // add the channel
                                this.accountData.channels().add(listChannel.getValue()); // save that the user has joined this channel
                                this.messages.put(listChannel, FXCollections.observableArrayList()); // create a message list for the channel
                            }
                        }
                        this.saveAccountData(); // save new channel data
                    });
                } else if (packet.type() == ServerPacketTypes.REMOVE_CHANNELS) {
                    // remove channels from the list of connected channels
                    ChannelList list = packet.getAs(ServerPacketTypes.REMOVE_CHANNELS);
                    Platform.runLater(() -> {
                        for (Identifier listChannel : list.getChannels()) {
                            if (this.channels.remove(listChannel)) { // remove the channel if it exists
                                this.messages.remove(listChannel); // remove the channel's messages
                                this.accountData.channels().remove(listChannel.getValue()); // remove the channel from the restore channel list
                            }
                        }
                        this.saveAccountData(); // save the new channel data to the config
                    });
                } else if (packet.type() == ServerPacketTypes.ADD_IMAGE_MESSAGE) {
                    // add an IMAGE message to a channel
                    AddImageMessage msg = packet.getAs(ServerPacketTypes.ADD_IMAGE_MESSAGE);
                    Platform.runLater(() -> {
                        ImageMessage img = new ImageMessage(msg.getTimestamp(), this.users.get(msg.getAuthorId()), msg.getWidth(), msg.getHeight(), msg.getContents(), msg.getSignature());
                        addMessage(msg.getChannel(), img);
                    });
                }
            }
        } catch (IOException e) {
            if (!this.closeConnection) {
                LOGGER.error("Connection error", e);
            } else {
                // we're shutting down, so the communications error is expected
                return;
            }
        }
        // if the error is unexpected, try to reconnect
        if (!this.closeConnection) {
            this.users.clear(); // remove all users - we don't know the state anymore
            if (this.screen != null) { // if we have an active window, mark it as OFFLINE - we can't do much right now
                Platform.runLater(this.screen::markOffline);
            }
            // try to reconnect
            if (!this.tryReconnect()) {
                // reconnection failed
                Alert alert = new Alert(Alert.AlertType.ERROR, "Server reconnection failed! Please log in again.");
                this.logout(primaryStage); // log out
                alert.showAndWait(); // show the alert
            }
        }
    }

    /**
     * Attempts to reconnect to the server
     *
     * @return whether reconnection was successful
     */
    private boolean tryReconnect() {
        LOGGER.info("Trying to reconnect to server");

        while (true) {
            PacketPipeline connect;
            try {
                Socket socket = new Socket();
                socket.bind(null);
                socket.connect(this.address); // connect to the server

                connect = PacketPipeline.createNetwork(Constants.PACKET_HEADER, socket);
                // send client hello
                connect.send(ClientPacketTypes.HELLO, new Hello(Constants.BRAND, Constants.VERSION, publicKey));

                // get the server response
                Packet<AuthenticationRequest> packet = connect.receivePacket();
                RSAPublicKey serverKey = packet.data().getServerKey(); // get the server public key
                String keyHash = CryptoHelper.sha256Hash(serverKey.getEncoded()); // calculate the hash of the id
                LOGGER.info("Server key id: {}", keyHash);
                String host = address.getHostString() + ":" + address.getPort();
                RSAPublicKey expectedKey = accountData.knownServers().get(host);
                if (!serverKey.equals(expectedKey)) { // verify that the key is the same as before
                    return false; // if not, immediately FAIL
                }

                // server key is correct, so let's respond
                LOGGER.info("Initializing ciphers");
                Cipher cipher = CryptoHelper.createRsaCipher();
                cipher.init(Cipher.DECRYPT_MODE, accountData.privateKey()); // setup decryption with our key
                byte[] encodedKey = cipher.doFinal(packet.data().getAuthData());
                cipher.init(Cipher.ENCRYPT_MODE, serverKey); // setup encryption with the server's key
                byte[] output = cipher.doFinal(encodedKey);

                // get the symmetric AES key used for communications
                LOGGER.info("Decoding symmetric (AES) key");
                SecretKey key;
                try {
                    key = CryptoHelper.decodeAesKey(encodedKey);
                } catch (InvalidKeySpecException e) {
                    return false;
                }

                // attempt to verify identity with the server
                LOGGER.info("Authenticating...");
                connect.send(ClientPacketTypes.AUTHENTICATE, new Authenticate(this.username, output));

                Packet<?> response = connect.receivePacket(); // get the server's response
                if (response.type() == ServerPacketTypes.AUTHENTICATION_SUCCESS) {
                    // we successfully connected, so re-initialize and go!
                    LOGGER.info("Successfully authenticated to the server");
                    AuthenticationSuccess success = response.getAs(ServerPacketTypes.AUTHENTICATION_SUCCESS);
                    this.initialize(connect.encryptWith(key), this.passKey, serverKey, publicKey, accountData, success.getUsers(), this.username, this.address);
                    Platform.runLater(this.screen::markOnline);
                    return true;
                } else if (response.type() == ServerPacketTypes.AUTHENTICATION_FAILURE) {
                    // the server explicitly rejected us, so stop trying to reconnect
                    String failure = response.getAs(ServerPacketTypes.AUTHENTICATION_FAILURE).getReason();
                    LOGGER.error("Server denied connection: {}", failure);
                    return false;
                } else {
                    // the server rejected us, so stop trying to reconnect
                    LOGGER.error("Server sent invalid id: " + response.type());
                    return false;
                }
            } catch (ConnectException ignored) {
            } catch (IOException e) {
                // I/O error is not fatal, so we can try again
                LOGGER.error("Communication I/O error", e);
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                // encryption failure so there's some deeper issue - fail reconnection
                LOGGER.error("Generic connection crypto failure", e);
                return false;
            }
            try {
                // try again every 2 seconds
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Adds a message to the given channel
     *
     * @param channel the channel the message was sent to
     * @param message the message contents
     */
    private void addMessage(Identifier channel, Message message) {
        if (!this.channels.contains(channel) || !this.messages.containsKey(channel))
            return; // if we aren't a part of the channel, ignore the message
        this.messages.get(channel).add(message); // add the message to the channel list
        if (this.trayIcon != null && !this.primaryStage.isFocused()) { // if we are not in focus, send a notification
            if (message.getType() == MessageType.TEXT) { // only text message notifications for now
                String message1 = ((TextMessage) message).getMessage();
                if (message1.length() > 64) {
                    message1 = message1.substring(0, 64 - 3) + "..."; // truncate the message if it is long
                }
                // send the notification
                this.trayIcon.displayMessage(this.getShortName(message.getAuthor()), message1, TrayIcon.MessageType.NONE);
            }
        }
    }

    /**
     * Saves the current account data to the configuration file (in encrypted form)
     */
    public void saveAccountData() {
        try {
            // setup data encryption
            this.aesCipher.init(Cipher.ENCRYPT_MODE, this.passKey);
            // encrypt the user data
            AccountData.EncryptedAccountData encrypted = this.accountData.encrypt(this.aesCipher);
            // update and save the config file
            this.config.updateAccountData(this.publicKey, encrypted); //todo: make it a username or key -> account map instead
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Sets/stores tbe active screen
     *
     * @param chatView the active screen
     */
    public void setView(ChatView chatView) {
        this.screen = chatView;
    }

    /**
     * Closes communications with the server.
     * Does not close the app
     */
    public void close() {
        this.closeConnection = true;
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (this.systemTray != null && this.trayIcon != null) {
                this.systemTray.remove(this.trayIcon);
                this.systemTray = null;
                this.trayIcon = null;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Closes server communications and the app
     *
     * @see #close()
     */
    public void shutdown() {
        this.close();
        Platform.exit();
    }

    /**
     * Opens the client trust management screen
     *
     * @param item user to select by default
     */
    public void openTrustScreen(@Nullable User item) {
        Stage stage = new Stage();
        UserTrustScreen trustScreen = new UserTrustScreen(this, stage); // open the screen
        trustScreen.select(item);
        stage.show(); // show the screen
    }

    /**
     * Sets the user's nickname
     *
     * @param item     the user to modify the nickname of
     * @param nickname the user's nickname
     */
    public void trustUser(User item, String nickname) {
        this.accountData.knownAccounts().put(item.key(), nickname);
        this.saveAccountData(); // save and store the nickname
        this.screen.refresh();
    }

    /**
     * Gets the friendly name of the given account
     *
     * @param account the account to get the name of
     * @return the account's friendly name
     */
    public String getName(MessageAuthor account) {
        return this.accountData.knownAccounts().getOrDefault(account.getPublicKey(), account.getLongIdName());
    }

    /**
     * Gets the short friendly name of the given account
     *
     * @param account the account to get the name of
     * @return the account's short friendly name
     */
    public String getShortName(MessageAuthor account) {
        return this.accountData.knownAccounts().getOrDefault(account.getPublicKey(), account.getShortIdName());
    }

    /**
     * Removes the nickname from the given account
     *
     * @param selected the account to remove the nickname of
     */
    public void revokeTrust(User selected) {
        this.accountData.knownAccounts().remove(selected.key());
        this.saveAccountData(); // record the removal of the nickname
        this.screen.refresh();
    }

    /**
     * Whether a nickname exists for the given account
     *
     * @param account the account to test
     * @return whether a nickname exists
     */
    public boolean isTrusted(MessageAuthor account) {
        return this.accountData.knownAccounts().containsKey(account.getPublicKey());
    }

    /**
     * Informs the server to remove this client from a given channel's listeners
     *
     * @param item the channel to leave
     */
    public void leaveChannel(Identifier item) {
        if (!this.channels.contains(item)) return; // if we aren't a part of the channel, cancel
        try {
            this.connection.send(ClientPacketTypes.LEAVE_CHANNELS, new ChannelList(item));
        } catch (IOException e) {
            LOGGER.error("Failed to leave channel", e);
        }
    }

    /**
     * Informs the server to add this client to a given channel's listeners
     *
     * @param channel the channel to join
     */
    public void joinChannel(Identifier channel) {
        if (this.channels.contains(channel)) return; // if we are already a part of the channel, cancel
        try {
            this.connection.send(ClientPacketTypes.JOIN_CHANNELS, new ChannelList(channel));
        } catch (IOException e) {
            LOGGER.error("Failed to join channel", e);
        }
    }

    /**
     * Signs the given integer array as a message
     *
     * @param pixels the raw data
     * @return the data's signature
     */
    public byte[] signMessage(int[] pixels) {
        byte[] arr = new byte[pixels.length * 4];
        ByteBuffer.wrap(arr).asIntBuffer().put(pixels); // too lazy to bother with bits...
        try {
            this.rsaSignature.update(arr);
            return this.rsaSignature.sign();
        } catch (SignatureException e) {
            throw new IllegalStateException(e);
        }
    }
}
