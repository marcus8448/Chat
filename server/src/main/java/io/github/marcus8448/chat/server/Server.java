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

package io.github.marcus8448.chat.server;

import io.github.marcus8448.chat.core.api.Constants;
import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.message.Message;
import io.github.marcus8448.chat.core.api.misc.Cell;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.packet.PacketType;
import io.github.marcus8448.chat.core.api.network.packet.ServerPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.server.AddMessage;
import io.github.marcus8448.chat.core.api.network.packet.server.SystemMessage;
import io.github.marcus8448.chat.core.api.network.packet.server.UserConnect;
import io.github.marcus8448.chat.core.api.network.packet.server.UserDisconnect;
import io.github.marcus8448.chat.server.network.ClientConnectionHandler;
import io.github.marcus8448.chat.server.network.ClientLoginConnectionHandler;
import io.github.marcus8448.chat.server.thread.ThreadPerTaskExecutor;
import io.github.marcus8448.chat.server.util.Users;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Closeable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Cell<Thread> mainThread = new Cell<>();
    public final ExecutorService executor;
    private final ExecutorService connectionExecutor;
    private final Signature rsaSignature = CryptoHelper.createRsaSignature();
    public final RSAPublicKey publicKey;
    public final RSAPrivateKey privateKey;
    private final List<ClientConnectionHandler> connectionHandlers = new ArrayList<>();
    private final Users users = new Users();
    private final ServerSocket socket;
    public volatile boolean shutdown = false;

    public Server(int port, RSAPublicKey publicKey, RSAPrivateKey privateKey) throws IOException {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        ExecutorService service;
        try {
            service = (ExecutorService) Executors.class.getMethod("newVirtualThreadPerTaskExecutor").invoke(null);
            LOGGER.info("Using virtual thread per task executor");
        } catch (Exception e) {
            service = new ThreadPerTaskExecutor();
        }
        try {
            this.rsaSignature.initSign(this.privateKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        this.connectionExecutor = service;
        this.executor = Executors.newSingleThreadExecutor(r -> mainThread.setValue(new Thread(r, "Server Main")));
        this.socket = new ServerSocket(port);
    }

    public void sendMessage(String message) {
        this.assertOnThread();
        long time = System.currentTimeMillis();
        byte[] sign;
        try {
            this.rsaSignature.update(message.getBytes(StandardCharsets.UTF_8));
            sign = this.rsaSignature.sign();
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
        this.sendToAll(ServerPacketTypes.SYSTEM_MESSAGE, new SystemMessage(time, message, sign));
    }

    public void launch() {
        Thread thread = new Thread(this::serverAdministration);
        thread.start();
        while (!this.socket.isClosed() && !this.shutdown) {
            try {
                Socket accepted = this.socket.accept();
                ClientLoginConnectionHandler connectionHandler = new ClientLoginConnectionHandler(
                        this,
                        PacketPipeline.createNetwork(
                                Constants.PACKET_HEADER,
                                accepted
                        )
                );
                this.executor.execute(() -> {
                    this.connectionHandlers.add(connectionHandler);
                    this.connectionExecutor.execute(connectionHandler);
                });
            } catch (Exception ignored) {
            }
        }
    }

    private void serverAdministration() {
        Scanner scanner = new Scanner(System.in);
        while (!this.shutdown && !this.executor.isShutdown()) {
            String[] s = scanner.nextLine().split(" ");
            String command = s[0];
            switch (command) {
                case "kick" -> {
                }
                case "exit" -> {
                    this.close();
                }
                default -> {
                    LOGGER.error("Invalid command!");
                }
            }
        }
    }

    public void updateConnection(ClientConnectionHandler oldHandler, ClientConnectionHandler newHandler, User user) {
        this.executor.execute(() -> {
            if (this.connectionHandlers.remove(oldHandler)) {
                this.sendToAll(ServerPacketTypes.USER_CONNECT, new UserConnect(user));
                this.sendMessage(user.getFormattedName() + " has joined the chat!");

                this.connectionHandlers.add(newHandler);
                this.connectionExecutor.submit(newHandler);
            } else {
                throw new RuntimeException("Failed to replace handler");
            }
        });
    }

    private void assertOnThread() {
        if (Thread.currentThread() != this.mainThread.getValue()) {
            throw new WrongThreadException();
        }
    }

    @Override
    public void close() {
        LOGGER.info("Server is shutting down");
        this.shutdown = true;
        List<ClientConnectionHandler> handlers = new ArrayList<>(this.connectionHandlers);
        for (ClientConnectionHandler handler : handlers) {
            handler.shutdown();
        }
        this.connectionExecutor.shutdown();
        this.executor.shutdown();
        try {
            this.socket.close();
        } catch (IOException ignored) {}
        LOGGER.info("Shutdown successful.");
    }

    public boolean isConnected(RSAPublicKey key) {
        return this.users.contains(key);
    }

    protected <Data extends NetworkedData> void sendToAll(PacketType<Data> type, Data data) {
        for (ClientConnectionHandler handler : this.connectionHandlers) {
            handler.send(type, data);
        }
    }

    public void receiveMessage(long time, User user, byte[] checksum, String message) {
        this.assertOnThread();
        this.sendToAll(ServerPacketTypes.ADD_MESSAGE, new AddMessage(time, user.sessionId(), message, checksum));
    }

    public @NotNull User createUser(String username, RSAPublicKey key, byte @Nullable [] icon) {
        this.assertOnThread();
        return this.users.createUser(username, key, icon);
    }

    public void disconnect(ClientConnectionHandler handler, User user) {
        this.assertOnThread();
        this.connectionHandlers.remove(handler);
        if (user != null) {
            this.users.remove(user);

            this.sendToAll(ServerPacketTypes.USER_DISCONNECT, new UserDisconnect(user.sessionId()));
            this.sendMessage(user.getFormattedName() + " has left the chat.");
        }
    }

    public Collection<User> getUsers() {
        return this.users.getUsers();
    }
}
