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
import io.github.marcus8448.chat.core.api.channel.Channel;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.misc.Cell;
import io.github.marcus8448.chat.core.api.misc.Identifier;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.packet.PacketType;
import io.github.marcus8448.chat.core.api.network.packet.ServerPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.common.ChannelList;
import io.github.marcus8448.chat.core.api.network.packet.server.*;
import io.github.marcus8448.chat.server.network.ClientConnectionHandler;
import io.github.marcus8448.chat.server.network.ClientLoginConnectionHandler;
import io.github.marcus8448.chat.server.thread.ConnectionThreadFactory;
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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Closeable {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * The main executor service
     */
    public final ExecutorService executor;
    /**
     * This server's public RSA key
     */
    public final RSAPublicKey publicKey;
    /**
     * This server's private RSA key
     */
    public final RSAPrivateKey privateKey;
    /**
     * The current main executor thread
     */
    private final Cell<Thread> mainThread = new Cell<>();
    /**
     * The executor service that manages all client connections
     */
    private final ExecutorService connectionExecutor;
    /**
     * RSA signature instance initialized with the server's private key
     */
    private final Signature rsaSignature = CryptoHelper.createRsaSignature();
    /**
     * Active client connections
     */
    private final List<ClientConnectionHandler> connectionHandlers = new ArrayList<>();
    /**
     * Active (online) users
     */
    private final Users users = new Users();
    /**
     * Map of channel names -> channels
     */
    private final Map<Identifier, Channel> channels = new HashMap<>();
    /**
     * The main (incoming connection) socket
     */
    private final ServerSocket socket;
    /**
     * Whether the server is/should be shutting down
     */
    public volatile boolean shutdown = false;

    public Server(int port, RSAPublicKey publicKey, RSAPrivateKey privateKey) throws IOException {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        ExecutorService service;
        try {
            // use virtual threads if possible
            service = (ExecutorService) Executors.class.getMethod("newVirtualThreadPerTaskExecutor").invoke(null);
            LOGGER.info("Using virtual thread executor");
        } catch (Exception e) {
            // unavailable, so use a normal executor
            service = Executors.newCachedThreadPool(new ConnectionThreadFactory());
            LOGGER.info("Using cached thread pool executor (JDK 19/20 preview features are not available)");
        }
        try {
            // initialize our signature
            this.rsaSignature.initSign(this.privateKey);
        } catch (InvalidKeyException e) {
            LOGGER.fatal("Failed to initialize signature with private key.", e);
            System.exit(0);
        }

        this.connectionExecutor = service;
        this.executor = Executors.newSingleThreadExecutor(r -> this.mainThread.setValue(new Thread(r, "Server Main")));
        this.socket = new ServerSocket(port);
        // add the default channel
        this.channels.put(Constants.BASE_CHANNEL, new Channel(Constants.BASE_CHANNEL));
    }

    /**
     * Send a SYSTEM message to all clients listening to a channel
     *
     * @param channel the channel to send to
     * @param message the message
     */
    public void sendMessage(Identifier channel, String message) {
        this.assertOnThread();
        long time = System.currentTimeMillis();
        byte[] sign;
        // sign the message
        try {
            this.rsaSignature.update(message.getBytes(StandardCharsets.UTF_8));
            sign = this.rsaSignature.sign();
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
        // send the message
        this.sendToChannel(channel, ServerPacketTypes.SYSTEM_MESSAGE, new SystemMessage(channel, time, message, sign));
    }

    /**
     * Sends the given packet to all clients listening to a channel
     *
     * @param channel the channel to send to
     * @param type    the type of packet
     * @param data    the packet contents
     * @param <Data>  the type of packet data
     */
    protected <Data extends NetworkedData> void sendToChannel(Identifier channel, PacketType<Data> type, Data data) {
        Channel channel1 = this.channels.get(channel);
        for (ClientConnectionHandler handler : this.connectionHandlers) { // iterate over all connections
            if (channel1.getParticipants().contains(handler.getUser())) { // check if user is in the channel
                handler.send(type, data); // send packet
            }
        }
    }

    public void launch() {
        // start the server admin system off-thread
//        Thread thread = new Thread(this::serverAdministration);
//        thread.start();

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

    /**
     * Accepts server administration commands via stdin
     */
    private void serverAdministration() {
        Scanner scanner = new Scanner(System.in);
        while (!this.shutdown && !this.executor.isShutdown()) {
            String[] s = scanner.nextLine().split(" ");
            String command = s[0];
            switch (command) {
                case "kick" -> {
                }
                case "exit", "close", "stop" -> this.close();
                default -> LOGGER.error("Invalid command!");
            }
        }
    }

    /**
     * Updates the state of a connection handler
     *
     * @param oldHandler the old connection handler
     * @param newHandler the upgraded handler
     * @param user       the new user
     */
    public void updateConnection(ClientConnectionHandler oldHandler, ClientConnectionHandler newHandler, User user) {
        this.executor.execute(() -> { // execute on main thread
            if (this.connectionHandlers.remove(oldHandler)) { // remove the old handler
                LOGGER.info("User " + user.getLongIdName() + " has logged in.");
                this.sendToAll(ServerPacketTypes.USER_CONNECT, new UserConnect(user)); // notify clients of user
                this.sendMessage(Constants.BASE_CHANNEL, user.getShortIdName() + " has joined the chat!");
                this.channels.get(Constants.BASE_CHANNEL).addParticipant(user); // add user to base channel

                // add new connection handler
                this.connectionHandlers.add(newHandler);
                // start the handler
                this.connectionExecutor.submit(newHandler);
            } else {
                throw new RuntimeException("Failed to replace handler");
            }
        });
    }

    /**
     * Verifies that code is running on the correct (main) thread
     *
     * @throws IllegalStateException if the thread is incorrect
     */
    private void assertOnThread() {
        if (Thread.currentThread() != this.mainThread.getValue()) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void close() {
        LOGGER.info("Server is shutting down");
        this.shutdown = true;
        List<ClientConnectionHandler> handlers = new ArrayList<>(this.connectionHandlers);
        for (ClientConnectionHandler handler : handlers) {
            handler.shutdown(); // stop all connections
        }
        this.connectionExecutor.shutdown();
        this.executor.shutdown();
        try {
            this.socket.close(); // stop accepting incoming connections
        } catch (IOException ignored) {
        }
        LOGGER.info("Shutdown successful.");
    }

    /**
     * @return whether the server can accept a user with the given key
     */
    public boolean canAccept(RSAPublicKey key) {
        return this.users.canAccept(key) && !key.equals(this.publicKey);
    }

    /**
     * Sends a packet to all available connections
     *
     * @param type   the packet type
     * @param data   the packet body
     * @param <Data> the type of the packet body
     */
    protected <Data extends NetworkedData> void sendToAll(PacketType<Data> type, Data data) {
        for (ClientConnectionHandler handler : this.connectionHandlers) {
            try {
                handler.send(type, data);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * @return the channel with the given id (or null if it does not exist)
     */
    protected Channel getChannel(Identifier id) {
        return this.channels.get(id);
    }

    /**
     * Sends a given message from a client to all subscribed clients
     *
     * @param channel  the channel to send to
     * @param time     when the message was received
     * @param user     the user that sent the message
     * @param checksum the signature of the contents
     * @param message  the message contents
     */
    public void receiveMessage(Identifier channel, long time, User user, byte[] checksum, String message) {
        this.assertOnThread();
        if (this.getChannel(channel).contains(user)) { // verify that the user can send to this channel
            // send the packet to the channel
            this.sendToChannel(channel, ServerPacketTypes.ADD_MESSAGE, new AddMessage(channel, time, user.sessionId(), message, checksum));
        }
    }

    /**
     * Creates a new user instance
     *
     * @param username the username of the new user
     * @param key      the public key of the user
     * @param icon     the user's profile picture icon
     * @return a new user instance
     */
    public @NotNull User createUser(Identifier username, RSAPublicKey key, byte @Nullable [] icon) {
        this.assertOnThread();
        return this.users.createUser(username, key, icon);
    }

    /**
     * Disconnects the given connection handler and user and propagates the change to other clients
     *
     * @param handler the handler to remove
     * @param user    the user to remove
     */
    public void disconnect(ClientConnectionHandler handler, User user) {
        this.assertOnThread();
        // remove the handler
        this.connectionHandlers.remove(handler);

        if (user != null) {
            // remove the user
            this.users.remove(user);
            for (Channel value : this.channels.values()) {
                // remove the user from channel subscriptions
                value.removeParticipant(user);
            }

            // tell all clients that a user left
            this.sendToAll(ServerPacketTypes.USER_DISCONNECT, new UserDisconnect(user.sessionId()));
            this.sendMessage(Constants.BASE_CHANNEL, user.getShortIdName() + " has left the chat.");
        }
    }

    public Collection<User> getUsers() {
        return this.users.getUsers();
    }

    /**
     * Removes a user from the given channels
     *
     * @param handler  the user's client connection
     * @param user     the user
     * @param channels the channels to remove
     */
    public void leaveChannels(ClientConnectionHandler handler, User user, Identifier[] channels) {
        // list of channels actually removed
        List<Identifier> successful = new ArrayList<>();
        for (Identifier channel : channels) {
            if (channel.equals(Constants.BASE_CHANNEL)) continue;
            // get the channel
            Channel channel1 = this.channels.get(channel);
            if (channel1 != null) { // check that it exists
                if (channel1.contains(user)) { // check that the user is a part of it
                    channel1.removeParticipant(user); // remove the user from it
                    successful.add(channel); // add to removed channels
                }
            }
        }
        // inform the client of the subscription changes
        handler.send(ServerPacketTypes.REMOVE_CHANNELS, new ChannelList(successful.toArray(new Identifier[0])));
    }

    /**
     * Adds a user to the given channels
     *
     * @param handler  the user's client connection
     * @param user     the user
     * @param channels the channels to add
     */
    public void joinChannels(ClientConnectionHandler handler, User user, Identifier[] channels) {
        List<Identifier> successful = new ArrayList<>(); // channels actually added
        for (Identifier channel : channels) {
            if (channel.equals(Constants.BASE_CHANNEL)) continue;
            // get or create the channel
            Channel channel1 = this.channels.computeIfAbsent(channel, Channel::new);
            if (!channel1.contains(user)) { // add the user if it is not a part of it
                channel1.addParticipant(user);
                successful.add(channel);
            }
        }
        // send the list of added channels
        handler.send(ServerPacketTypes.ADD_CHANNELS, new ChannelList(successful.toArray(new Identifier[0])));
    }

    /**
     * Propagates an image message to all subscribed clients
     *
     * @param channel   the channel to send the image to
     * @param l         when the message was received
     * @param user      the user that sent the image
     * @param signature the image data signature
     * @param image     the image data
     */
    public void receiveFileMessage(Identifier channel, long l, User user, byte[] signature, byte[] image) {
        this.assertOnThread();
        if (this.getChannel(channel).contains(user)) { // verify that the user can send to the channel
            if (image.length < 1048576 * 16) { // 16MB limit
                // send the image
                this.sendToChannel(channel, ServerPacketTypes.ADD_IMAGE_MESSAGE, new AddFileMessage(channel, l, user.sessionId(), image, signature));
            }
        }
    }
}
