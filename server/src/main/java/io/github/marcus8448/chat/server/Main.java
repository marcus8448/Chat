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
import io.github.marcus8448.chat.core.api.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.connection.BinaryOutput;
import io.github.marcus8448.chat.core.api.connection.PacketPipeline;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.PacketTypes;
import io.github.marcus8448.chat.core.network.packet.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;
    public static void main(String[] args) throws IOException, InvalidKeySpecException {
        LOGGER.info("Loading Chat Server v{}", Constants.VERSION);
        File pubKF = new File("public.key");
        File privKF = new File("private.key");
        if (!pubKF.exists() || !privKF.exists()) {
            LOGGER.warn("No existing server keypair found. Generating...");
            pubKF.delete();
            privKF.delete();
            KeyPair keyPair = CryptoHelper.RSA_KEY_GENERATOR.generateKeyPair();
            publicKey = (RSAPublicKey) keyPair.getPublic();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            LOGGER.info("Server keypair successfully generated. ID: {}", CryptoHelper.sha256Hash(keyPair.getPublic().getEncoded()));
            Files.write(pubKF.toPath(), publicKey.getEncoded());
            Files.write(privKF.toPath(), privateKey.getEncoded());
        } else {
            publicKey = (RSAPublicKey) CryptoHelper.RSA_KEY_FACTORY.generatePublic(new X509EncodedKeySpec(Files.readAllBytes(pubKF.toPath())));
            privateKey = (RSAPrivateKey) CryptoHelper.RSA_KEY_FACTORY.generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(privKF.toPath())));
        }
        Thread connectionHandler = new Thread(() -> connectHandler(Constants.PORT), "Connection Handler");
        connectionHandler.start();

    }

    private static void connectHandler(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            while (!socket.isClosed()) {
                try {
                    Socket accepted = socket.accept();
                    new Thread(() -> {
                        try {
                            loginHandler(accepted);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, "Login handler").start();
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Incoming connection failure", e);
        }
    }

    private static void loginHandler(Socket socket) throws IOException, IllegalBlockSizeException, BadPaddingException {
        PacketPipeline connection = PacketPipeline.createBasic(Constants.PACKET_HEADER, BinaryInput.stream(socket.getInputStream()), BinaryOutput.stream(socket.getOutputStream()));

        System.out.println("HANDLE");
        while (!socket.isClosed() && socket.isConnected()) {
            Packet<?> packet = connection.receivePacket();
            PacketType<?> type = packet.type();
            if (type == PacketTypes.CLIENT_HELLO) {
                ClientHello hello = (ClientHello) packet.data();
                if (!Objects.equals(hello.getClientVersion(), Constants.VERSION)) {
                    connection.send(PacketTypes.SERVER_AUTH_RESPONSE, new ServerAuthResponse(false, "Version mismatch!"));
                    socket.close();
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

                try {
                    connection.send(PacketTypes.SERVER_AUTH_REQUEST, new ServerAuthRequest(publicKey, rsaCipher.doFinal(bytes)));
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    throw new RuntimeException(e);
                }

                Packet<ClientAuthResponse> packet1 = connection.receivePacket();
                ClientAuthResponse auth = packet1.data();
                try {
                    rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
                byte[] bytes1 = rsaCipher.doFinal(auth.getData());

                if (Arrays.equals(bytes1, bytes)) {
                    connection.send(PacketTypes.SERVER_AUTH_RESPONSE, new ServerAuthResponse(true, null));
                    System.out.println(auth.getUsername());
                }
            }
        }
    }
}