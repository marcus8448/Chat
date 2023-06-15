package io.github.marcus8448.chat.server.network;

import io.github.marcus8448.chat.core.api.Constants;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.network.PacketHandler;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.network.connection.BinaryOutput;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.PacketTypes;
import io.github.marcus8448.chat.core.network.packet.*;
import io.github.marcus8448.chat.core.user.User;
import io.github.marcus8448.chat.server.Server;
import io.github.marcus8448.chat.server.network.ClientConnectionHandler;
import io.github.marcus8448.chat.server.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

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
            while (this.pipeline.isOpen()) {
                Packet<?> packet = this.pipeline.receivePacket();
                PacketType<?> type = packet.type();
                if (type == PacketTypes.CLIENT_HELLO) {
                    ClientHello hello = (ClientHello) packet.data();
                    LOGGER.trace("Hello from '{}' version {}", hello.getClientBrand(), hello.getClientVersion());
                    if (!Objects.equals(hello.getClientVersion(), Constants.VERSION)) {
                        LOGGER.warn("Rejected client due to version mismatch [client: {} | server: {}]", hello.getClientVersion(), Constants.VERSION);
                        this.pipeline.send(PacketTypes.SERVER_AUTH_RESPONSE, new ServerAuthResponse(false, "Version mismatch!"));
                        this.pipeline.close();
                        return;
                    }
                    Random rand = new Random(); //fixme: secure random
                    byte[] bytes = new byte[64];
                    rand.nextBytes(bytes);
                    Cipher rsaCipher = CryptoHelper.createRsaCipher();
                    try {
                        rsaCipher.init(Cipher.ENCRYPT_MODE, hello.getKey());
                    } catch (InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }

                    LOGGER.trace("Sending authentication data");
                    try {
                        this.pipeline.send(PacketTypes.SERVER_AUTH_REQUEST, new ServerAuthRequest(this.server.publicKey, rsaCipher.doFinal(bytes)));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        throw new RuntimeException(e);
                    }

                    Packet<ClientAuthResponse> packet1 = this.pipeline.receivePacket();
                    ClientAuthResponse auth = packet1.data();
                    try {
                        rsaCipher.init(Cipher.DECRYPT_MODE, this.server.privateKey);
                    } catch (InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }
                    byte[] bytes1 = rsaCipher.doFinal(auth.getData());

                    if (Arrays.equals(bytes1, bytes)) {
                        if (!this.server.isConnected(hello.getKey())) {
                            LOGGER.info("Client successfully connected!");
                            this.server.executor.execute(() -> {
                                User user = this.server.createUser(auth.getUsername(), hello.getKey(), null);
                                try {
                                    this.server.updateConnection(this, new ClientMainConnectionHandler(this.server, this.pipeline.encryptWith(hello.getKey(), this.server.privateKey), user));
                                    this.pipeline.send(PacketTypes.SERVER_AUTH_RESPONSE, new ServerAuthResponse(true, null));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            return;
                        } else {
                            LOGGER.error("User with the same key already connected");
                            this.pipeline.send(PacketTypes.SERVER_AUTH_RESPONSE, new ServerAuthResponse(false, "Client with this public key already connected."));
                            this.pipeline.close();
                        }
                    } else {
                        LOGGER.warn("Client failed identity verification");
                        this.pipeline.send(PacketTypes.SERVER_AUTH_RESPONSE, new ServerAuthResponse(false, "Identity verification failed."));
                        this.pipeline.close();
                    }
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <Data extends NetworkedData> void send(Packet<Data> packet) {
        try {
            this.pipeline.send(packet.type(), packet.data());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
