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
import io.github.marcus8448.chat.core.api.Constants;
import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.message.Message;
import io.github.marcus8448.chat.core.api.message.MessageType;
import io.github.marcus8448.chat.core.api.message.TextMessage;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.ServerPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.server.AddMessage;
import io.github.marcus8448.chat.core.api.network.packet.server.SystemMessage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client extends Application implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Cipher aesCipher = CryptoHelper.createAesCipher();
    private final Signature rsaSignature = CryptoHelper.createRsaSignature();

    public Config config;
    private ChatView screen;
    public PacketPipeline connection;
    private SecretKey passKey;
    private RSAPublicKey publicKey;
    private RSAPublicKey serverPubKey;
    private AccountData accountData;
    public final ObservableList<Message> messages = FXCollections.observableArrayList();
    public final Map<Integer, User> users = new HashMap<>();

    private Stage primaryStage;
    private SystemTray systemTray;
    private TrayIcon trayIcon;

    public Client() {}

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
                LOGGER.error("Failed to load icon image!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        primaryStage.show();
    }

    public void logout(Stage stage) {
        try {
            this.connection.close();
        } catch (IOException ignored) {}
        this.connection = null;
        this.passKey = null;
        this.serverPubKey = null;
        this.accountData = null;
        this.messages.clear();
        this.users.clear();
        if (this.trayIcon != null && this.systemTray != null) {
            this.systemTray.remove(this.trayIcon);
            this.trayIcon = null;
            this.systemTray = null;
        }
        stage.hide();
        this.beginLoginProcess(stage);
    }

    public void initialize(PacketPipeline pipeline, SecretKey passKey, RSAPublicKey serverKey, RSAPublicKey publicKey, SecretKey key, @NotNull AccountData data, List<User> users) {
        this.connection = pipeline;
        this.passKey = passKey;
        this.publicKey = publicKey;
        this.serverPubKey = serverKey;
        this.accountData = data;
        for (User user : users) {
            this.users.put(user.sessionId(), user);
        }
        RSAPrivateKey privateKey = this.accountData.privateKey();
        try {
            this.rsaSignature.initSign(privateKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        Thread thread = new Thread(this, "Client Main");
        thread.start();
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
                        LOGGER.info("action: {}, param: {}, modifiers: {}", l.getActionCommand(), l.paramString(), l.getModifiers());
                    });
                    this.systemTray.add(this.trayIcon);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load system tray");
        }
    }

    public byte[] signMessage(String contents) {
        try {
            this.rsaSignature.update(contents.getBytes(StandardCharsets.UTF_8));
            return this.rsaSignature.sign();
        } catch (SignatureException e) {
            throw new RuntimeException(e);
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
                        addMessage(text);
                    });
                } else if (packet.type() == ServerPacketTypes.USER_CONNECT) {
                    User user = packet.getAs(ServerPacketTypes.USER_CONNECT).getUser();
                    Platform.runLater(() -> this.users.put(user.sessionId(), user));
                } else if (packet.type() == ServerPacketTypes.USER_DISCONNECT) {
                    int id = packet.getAs(ServerPacketTypes.USER_DISCONNECT).getId();
                    Platform.runLater(() -> this.users.remove(id));
                } else if (packet.type() == ServerPacketTypes.SYSTEM_MESSAGE) {
                    SystemMessage systemMessage = packet.getAs(ServerPacketTypes.SYSTEM_MESSAGE);
                    //todo
                }
            }
        } catch (IOException e) {
            if (e.getMessage().equals("Socket closed")) {
                return;
            }
            LOGGER.error("Connection error", e);
        }
    }

    private void addMessage(Message message) {
        this.messages.add(message);
        if (this.trayIcon != null && !this.primaryStage.isFocused()) {
            if (message.getType() == MessageType.TEXT) {
                String message1 = ((TextMessage) message).getMessage();
                if (message1.length() > 64) {
                    message1 = message1.substring(0, 64 - 3) + "...";
                }
                this.trayIcon.displayMessage(message.getAuthor().getFormattedName(), message1, TrayIcon.MessageType.NONE);
            }
        }
    }

    public void saveAccountData() {
        try {
            this.aesCipher.init(Cipher.ENCRYPT_MODE, this.passKey);
            AccountData.EncryptedAccountData encrypted = this.accountData.encrypt(this.aesCipher);
            this.config.updateAccountData(this.publicKey, encrypted); //todo: make it a username -> account map instead
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setView(ChatView chatView) {
        this.screen = chatView;
    }

    public void close() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (IOException ignored) {
        }
    }

    public void shutdown() {
        this.close();
        Platform.exit();
    }
}
