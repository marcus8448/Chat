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

package io.github.marcus8448.chat.server.network;

import io.github.marcus8448.chat.core.api.Constants;
import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.packet.ClientPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.PacketType;
import io.github.marcus8448.chat.core.api.network.packet.ServerPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.client.Authenticate;
import io.github.marcus8448.chat.core.api.network.packet.client.Hello;
import io.github.marcus8448.chat.core.api.network.packet.server.AuthenticationFailure;
import io.github.marcus8448.chat.core.api.network.packet.server.AuthenticationRequest;
import io.github.marcus8448.chat.core.api.network.packet.server.AuthenticationSuccess;
import io.github.marcus8448.chat.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Objects;

public class ClientLoginConnectionHandler implements ClientConnectionHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Server server;
    private final PacketPipeline pipeline;

    public ClientLoginConnectionHandler(Server server, PacketPipeline pipeline) {
        this.server = server;
        this.pipeline = pipeline;
    }

    @Override
    public <Data extends NetworkedData> void handle(Packet<Data> packet) {
        throw new UnsupportedOperationException("Login");
    }

    @Override
    public void run() {
        try {
            while (!this.server.shutdown && this.pipeline.isOpen()) {
                Packet<?> packet = this.pipeline.receivePacket();
                PacketType<?> type = packet.type();
                if (type == ClientPacketTypes.HELLO) {
                    Hello hello = (Hello) packet.data();
                    LOGGER.trace("Hello from '{}' version {}", hello.getBrand(), hello.getVersion());
                    if (!Objects.equals(hello.getVersion(), Constants.VERSION)) {
                        LOGGER.warn("Rejected client due to version mismatch [client: {} | server: {}]", hello.getVersion(), Constants.VERSION);
                        this.pipeline.send(ServerPacketTypes.AUTHENTICATION_FAILURE, new AuthenticationFailure("Version mismatch!"));
                        this.pipeline.close();
                        return;
                    }
                    SecretKey secretKey = CryptoHelper.AES_KEY_GENERATOR.generateKey();
                    Cipher rsaCipher = CryptoHelper.createRsaCipher();
                    try {
                        rsaCipher.init(Cipher.ENCRYPT_MODE, hello.getKey());
                    } catch (InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }

                    LOGGER.trace("Sending authentication data");
                    byte[] encoded = secretKey.getEncoded();
                    try {
                        this.pipeline.send(ServerPacketTypes.AUTHENTICATION_REQUEST, new AuthenticationRequest(this.server.publicKey, rsaCipher.doFinal(encoded)));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        throw new RuntimeException(e);
                    }

                    Packet<Authenticate> packet1 = this.pipeline.receivePacket();
                    Authenticate auth = packet1.data();
                    try {
                        rsaCipher.init(Cipher.DECRYPT_MODE, this.server.privateKey);
                    } catch (InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }
                    byte[] bytes1 = rsaCipher.doFinal(auth.getData());

                    if (Arrays.equals(bytes1, encoded)) {
                        if (this.server.canAccept(hello.getKey())) {
                            LOGGER.info("New client successfully connected");
                            this.server.executor.execute(() -> {
                                User user = this.server.createUser(auth.getUsername(), hello.getKey(), null);
                                try {
                                    this.server.updateConnection(this, new ClientMainConnectionHandler(this.server, this.pipeline.encryptWith(secretKey), user), user);
                                    this.pipeline.send(ServerPacketTypes.AUTHENTICATION_SUCCESS, new AuthenticationSuccess(this.server.getUsers()));
                                } catch (IOException e) {
                                    LOGGER.error(e);
                                }
                            });
                            return;
                        } else {
                            LOGGER.error("User with the same key already connected");
                            this.pipeline.send(ServerPacketTypes.AUTHENTICATION_FAILURE, new AuthenticationFailure("Client with this public key already connected."));
                            this.pipeline.close();
                        }
                    } else {
                        LOGGER.warn("Client failed identity verification");
                        this.pipeline.send(ServerPacketTypes.AUTHENTICATION_FAILURE, new AuthenticationFailure("Identity verification failed."));
                        this.pipeline.close();
                    }
                } else {
                    LOGGER.error("Client sent non-hello packet - closing connection");
                    this.shutdown();
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to accept client login", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.pipeline.close();
            server.executor.execute(() -> server.disconnect(this, null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <Data extends NetworkedData> void send(PacketType<Data> type, Data data) {
        try {
            this.pipeline.send(type, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
