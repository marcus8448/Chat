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
    public final Map<Identifier, ObservableList<Message>> messages = new HashMap<>();
    public final ObservableMap<Integer, User> users = FXCollections.observableHashMap();
    public final ObservableList<User> userList = FXCollections.observableArrayList();
    private final Cipher aesCipher = CryptoHelper.createAesCipher();
    private final Signature rsaSignature = CryptoHelper.createRsaSignature();
    public volatile boolean closeConnection = false;
    public Config config;
    public PacketPipeline connection;
    public ObservableList<Identifier> channels = FXCollections.observableArrayList();
    private ChatView screen;
    private SecretKey passKey;
    private RSAPublicKey publicKey;
    private RSAPublicKey serverPubKey;
    private AccountData accountData;
    private Stage primaryStage;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private InetSocketAddress address;
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
        this.config = Config.load(new File(parameters.getNamed().getOrDefault("config", "chat.json")));
        this.primaryStage = primaryStage;
        this.beginLoginProcess(primaryStage);
    }

    private void beginLoginProcess(Stage primaryStage) {
        LoginScreen loginScreen = new LoginScreen(this, primaryStage);
        try (InputStream iconStream = Client.class.getClassLoader().getResourceAsStream("icon.png")) {
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            } else {
                LOGGER.error("Missing icon!");
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load icon image", e);
        }
        primaryStage.show();
    }

    public void logout(Stage stage) {
        this.closeConnection = true;
        try {
            this.connection.close();
        } catch (IOException ignored) {
        }
        this.channels.clear();
        this.connection = null;
        this.passKey = null;
        this.serverPubKey = null;
        this.accountData = null;
        this.messages.clear();
        this.address = null;
        this.users.clear();
        this.username = null;
        if (this.trayIcon != null && this.systemTray != null) {
            this.systemTray.remove(this.trayIcon);
            this.trayIcon = null;
            this.systemTray = null;
        }
        stage.hide();
        this.beginLoginProcess(stage);
    }

    public void initialize(PacketPipeline pipeline, SecretKey passKey, RSAPublicKey serverKey, RSAPublicKey publicKey, @NotNull AccountData data, List<User> users, Identifier username, InetSocketAddress address) {
        this.connection = pipeline;
        this.closeConnection = false;
        this.address = address;
        this.passKey = passKey;
        this.publicKey = publicKey;
        this.serverPubKey = serverKey;
        this.accountData = data;
        this.username = username;
        this.messages.putIfAbsent(Constants.BASE_CHANNEL, FXCollections.observableArrayList());
        if (!this.channels.contains(Constants.BASE_CHANNEL)) this.channels.add(0, Constants.BASE_CHANNEL);
        for (User user : users) {
            this.users.put(user.sessionId(), user);
        }
        RSAPrivateKey privateKey = this.accountData.privateKey();
        try {
            this.rsaSignature.initSign(privateKey);
        } catch (InvalidKeyException e) {
            LOGGER.fatal("Failed to initialize signature", e);
            this.shutdown();
            return;
        }

        Thread thread = new Thread(this, "Client Main");
        thread.start();

        try {
            this.connection.send(ClientPacketTypes.JOIN_CHANNELS, new ChannelList(this.accountData.channels().stream().map(Identifier::create).toArray(Identifier[]::new)));
        } catch (Exception e) {
            LOGGER.fatal("Failed to request channels", e);
            this.shutdown();
            return;
        }

        if (this.trayIcon == null) {
            try (InputStream resourceAsStream = Client.class.getClassLoader().getResourceAsStream("icon.png")) {
                if (resourceAsStream != null) {
                    if (SystemTray.isSupported()) {
                        this.systemTray = SystemTray.getSystemTray();
                        this.trayIcon = new TrayIcon(ImageIO.read(resourceAsStream));
                        MenuItem exit = new MenuItem("Exit");
                        exit.addActionListener(l -> Platform.runLater(this::shutdown));
                        PopupMenu popup = new PopupMenu("Chat v" + Constants.VERSION);
                        popup.add(exit);
                        this.trayIcon.setToolTip("Chat v" + Constants.VERSION);
                        this.trayIcon.setPopupMenu(popup);
                        this.trayIcon.setImageAutoSize(true);
                        this.trayIcon.addActionListener(l -> {
                            if (l.getModifiers() == 0 || l.getModifiers() == InputEvent.BUTTON1_DOWN_MASK) { // notification click on windows
                                Platform.runLater(() -> {
                                    if (!this.primaryStage.isFocused()) {
                                        this.primaryStage.requestFocus();
                                    }
                                });
                            }
                        });
                        this.systemTray.add(this.trayIcon);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load system tray");
            }
        }
    }

    public byte[] signMessage(String contents) {
        try {
            this.rsaSignature.update(contents.getBytes(StandardCharsets.UTF_8));
            return this.rsaSignature.sign();
        } catch (SignatureException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void run() {
        try {
            while (this.connection.isOpen()) {
                Packet<?> packet = this.connection.receivePacket();
                LOGGER.info("Received packet : " + packet.type());
                if (packet.type() == ServerPacketTypes.ADD_MESSAGE) {
                    AddMessage addMessage = packet.getAs(ServerPacketTypes.ADD_MESSAGE);
                    Platform.runLater(() -> {
                        TextMessage text = Message.text(addMessage.getTimestamp(), this.users.get(addMessage.getAuthorId()), addMessage.getContents(), addMessage.getSignature());
                        addMessage(addMessage.getChannel(), text);
                    });
                } else if (packet.type() == ServerPacketTypes.USER_CONNECT) {
                    User user = packet.getAs(ServerPacketTypes.USER_CONNECT).getUser();
                    Platform.runLater(() -> this.users.put(user.sessionId(), user));
                } else if (packet.type() == ServerPacketTypes.USER_DISCONNECT) {
                    int id = packet.getAs(ServerPacketTypes.USER_DISCONNECT).getId();
                    Platform.runLater(() -> this.users.remove(id));
                } else if (packet.type() == ServerPacketTypes.SYSTEM_MESSAGE) {
                    SystemMessage systemMessage = packet.getAs(ServerPacketTypes.SYSTEM_MESSAGE);
                    Platform.runLater(() -> {
                        TextMessage text = Message.text(systemMessage.getTimestamp(), MessageAuthor.system(this.serverPubKey), systemMessage.getContents(), systemMessage.getSignature());
                        addMessage(systemMessage.getChannel(), text);
                    });
                } else if (packet.type() == ServerPacketTypes.ADD_CHANNELS) {
                    ChannelList list = packet.getAs(ServerPacketTypes.ADD_CHANNELS);
                    Identifier[] listChannels = list.getChannels();
                    Platform.runLater(() -> {
                        for (Identifier listChannel : listChannels) {
                            if (!this.channels.contains(listChannel)) {
                                this.channels.add(listChannel);
                                this.accountData.channels().add(listChannel.getValue());
                                this.messages.put(listChannel, FXCollections.observableArrayList());
                            }
                        }
                        this.saveAccountData();
                    });
                } else if (packet.type() == ServerPacketTypes.REMOVE_CHANNELS) {
                    ChannelList list = packet.getAs(ServerPacketTypes.REMOVE_CHANNELS);
                    Identifier[] listChannels = list.getChannels();
                    Platform.runLater(() -> {
                        for (Identifier listChannel : listChannels) {
                            if (this.channels.remove(listChannel)) {
                                this.messages.remove(listChannel);
                                this.accountData.channels().remove(listChannel.getValue());
                            }
                        }
                        this.saveAccountData();
                    });
                } else if (packet.type() == ServerPacketTypes.ADD_IMAGE_MESSAGE) {
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
                return;
            }
        }
        if (!this.closeConnection) {
            this.users.clear();
            if (this.screen != null) {
                Platform.runLater(this.screen::markOffline);
            }
            if (!this.tryReconnect()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Server reconnection failed! Please log in again.");
                this.logout(primaryStage);
                alert.showAndWait();
            }
        }
    }

    private boolean tryReconnect() {
        LOGGER.info("Trying to reconnect to server");

        while (true) {
            PacketPipeline connect;
            try {
                Socket socket = new Socket();
                socket.bind(null);
                socket.connect(this.address);
                connect = PacketPipeline.createNetwork(Constants.PACKET_HEADER, socket);
                connect.send(ClientPacketTypes.HELLO, new Hello(Constants.BRAND, Constants.VERSION, publicKey));

                Packet<AuthenticationRequest> packet = connect.receivePacket();
                RSAPublicKey serverKey = packet.data().getServerKey();
                String keyHash = CryptoHelper.sha256Hash(serverKey.getEncoded());
                LOGGER.info("Server key id: {}", keyHash);
                String host = address.getHostString() + ":" + address.getPort();
                RSAPublicKey expectedKey = accountData.knownServers().get(host);
                if (!serverKey.equals(expectedKey)) {
                    return false; // immediately FAIL
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
                    return false;
                }

                LOGGER.info("Authenticating...");
                connect.send(ClientPacketTypes.AUTHENTICATE, new Authenticate(this.username, output));
                Packet<?> response = connect.receivePacket();
                if (response.type() == ServerPacketTypes.AUTHENTICATION_SUCCESS) {
                    LOGGER.info("Successfully authenticated to the server");
                    AuthenticationSuccess success = response.getAs(ServerPacketTypes.AUTHENTICATION_SUCCESS);
                    this.initialize(connect.encryptWith(key), this.passKey, serverKey, publicKey, accountData, success.getUsers(), this.username, this.address);
                    Platform.runLater(this.screen::markOnline);
                    return true;
                } else if (response.type() == ServerPacketTypes.AUTHENTICATION_FAILURE) {
                    String failure = response.getAs(ServerPacketTypes.AUTHENTICATION_FAILURE).getReason();
                    LOGGER.error("Server denied connection: {}", failure);
                    return false;
                } else {
                    LOGGER.error("Server sent invalid id: " + response.type());
                    return false;
                }
            } catch (ConnectException ignored) {
            } catch (IOException e) {
                LOGGER.error("Communication I/O error", e);
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                LOGGER.error("Generic connection crypto failure", e);
                return false;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void addMessage(Identifier channel, Message message) {
        if (!this.channels.contains(channel) || !this.messages.containsKey(channel)) return;
        this.messages.get(channel).add(message);
        if (this.trayIcon != null && !this.primaryStage.isFocused()) {
            if (message.getType() == MessageType.TEXT) {
                String message1 = ((TextMessage) message).getMessage();
                if (message1.length() > 64) {
                    message1 = message1.substring(0, 64 - 3) + "...";
                }
                this.trayIcon.displayMessage(this.getShortName(message.getAuthor()), message1, TrayIcon.MessageType.NONE);
            }
        }
    }

    public void saveAccountData() {
        try {
            this.aesCipher.init(Cipher.ENCRYPT_MODE, this.passKey);
            AccountData.EncryptedAccountData encrypted = this.accountData.encrypt(this.aesCipher);
            this.config.updateAccountData(this.publicKey, encrypted); //todo: make it a username -> account map instead
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setView(ChatView chatView) {
        this.screen = chatView;
    }

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

    public void shutdown() {
        this.close();
        Platform.exit();
    }

    public void openTrustScreen(@Nullable User item) {
        Stage stage = new Stage();
        UserTrustScreen trustScreen = new UserTrustScreen(this, stage);
        trustScreen.select(item);
        stage.show();
    }

    public void trustUser(User item, String nickname) {
        this.accountData.knownAccounts().put(item.key(), nickname);
        this.saveAccountData();
        this.screen.refresh();
    }

    public String getName(MessageAuthor account) {
        return this.accountData.knownAccounts().getOrDefault(account.getPublicKey(), account.getLongIdName());
    }

    public String getShortName(MessageAuthor account) {
        return this.accountData.knownAccounts().getOrDefault(account.getPublicKey(), account.getShortIdName());
    }

    public void revokeTrust(User selected) {
        this.accountData.knownAccounts().remove(selected.key());
        this.saveAccountData();
        this.screen.refresh();
    }

    public boolean isTrusted(MessageAuthor account) {
        return this.accountData.knownAccounts().containsKey(account.getPublicKey());
    }

    public void leaveChannel(Identifier item) {
        if (!this.channels.contains(item)) return;
        try {
            this.connection.send(ClientPacketTypes.LEAVE_CHANNELS, new ChannelList(item));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void joinChannel(Identifier channel) {
        if (this.channels.contains(channel)) return;
        try {
            this.connection.send(ClientPacketTypes.JOIN_CHANNELS, new ChannelList(channel));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
