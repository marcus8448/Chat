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

import io.github.marcus8448.chat.core.Cell;
import io.github.marcus8448.chat.core.api.Constants;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.message.Message;
import io.github.marcus8448.chat.core.network.PacketTypes;
import io.github.marcus8448.chat.core.network.packet.NewMessage;
import io.github.marcus8448.chat.core.network.packet.Packet;
import io.github.marcus8448.chat.core.user.User;
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
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Closeable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Cell<Thread> mainThread = new Cell<>();
    public final ExecutorService executor;
    private final ExecutorService connectionExecutor;
    public final RSAPublicKey publicKey;
    public final RSAPrivateKey privateKey;
    private final List<ClientConnectionHandler> connectionHandlers = new ArrayList<>();
    private final Users users = new Users();

    public Server(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        ExecutorService service;
        try {
            service = (ExecutorService) Executors.class.getMethod("newVirtualThreadPerTaskExecutor").invoke(null);
            LOGGER.info("Using virtual thread per task executor");
        } catch (Exception e) {
            service = new ThreadPerTaskExecutor();
        }
        this.connectionExecutor = service;
        this.executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Server Main"));
    }

    public void sendMessage(Message message) {

    }

    public void launch(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            while (!socket.isClosed()) {
                if (this.executor.isShutdown()) socket.close();
                try {
                    Socket accepted = socket.accept();
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
        } catch (IOException e) {
            throw new RuntimeException("Incoming connection failure", e);
        }
    }

    public void updateConnection(ClientConnectionHandler oldHandler, ClientConnectionHandler newHandler) {
        this.executor.execute(() -> {
            if (this.connectionHandlers.remove(oldHandler)) {
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
        for (ClientConnectionHandler handler : this.connectionHandlers) {
            handler.shutdown();
        }
        this.connectionExecutor.shutdown();
        this.executor.shutdown();
    }

    public boolean isConnected(RSAPublicKey key) {
        return this.users.contains(key);
    }

    public void receiveMessage(long time, User user, byte[] checksum, String message) {
        for (ClientConnectionHandler handler : this.connectionHandlers) {
            handler.send(new Packet<>(PacketTypes.NEW_MESSAGE, new NewMessage(time, user.sessionId(), message, checksum)));
        }
    }

    public @NotNull User createUser(String username, RSAPublicKey key, byte @Nullable [] icon) {
        this.assertOnThread();
        return this.users.createUser(username, key, icon);
    }
}
