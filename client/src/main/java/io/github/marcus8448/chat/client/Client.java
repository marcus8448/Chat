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
import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.message.Message;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.ServerPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.server.AddMessage;
import io.github.marcus8448.chat.core.api.network.packet.server.SystemMessage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
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
    private final Cipher rsaCipher = CryptoHelper.createRsaCipher();
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

    public Client() {}

    @Override
    public void start(Stage primaryStage) {
        Parameters parameters = this.getParameters();
        this.config = Config.load(new File(parameters.getNamed().getOrDefault("config", "chat.json")));
        LoginScreen loginScreen = new LoginScreen(this, primaryStage);
        primaryStage.show();
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
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
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
                    Platform.runLater(() -> this.messages.add(Message.text(addMessage.getTimestamp(), this.users.get(addMessage.getAuthorId()), addMessage.getContents(), addMessage.getSignature())));
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
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            this.connection.close();
        } catch (IOException ignored) {
        }
    }
}
