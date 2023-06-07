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

import io.github.marcus8448.chat.core.Constants;
import io.github.marcus8448.chat.core.crypto.CryptoConstants;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.PacketTypes;
import io.github.marcus8448.chat.core.network.connection.ConnectionInput;
import io.github.marcus8448.chat.core.network.connection.ConnectionOutput;
import io.github.marcus8448.chat.core.network.connection.NetworkConnection;
import io.github.marcus8448.chat.core.network.packet.*;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

public class Main {
    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;
    public static void main(String[] args) throws IOException, InvalidKeySpecException {
        File pubKF = new File("public.key");
        File privKF = new File("private.key");
        if (!pubKF.exists() || !privKF.exists()) {
            pubKF.delete();
            privKF.delete();
            KeyPair keyPair = CryptoConstants.RSA_KEY_GENERATOR.generateKeyPair();
            publicKey = (RSAPublicKey) keyPair.getPublic();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            Files.write(pubKF.toPath(), publicKey.getEncoded());
            Files.write(privKF.toPath(), privateKey.getEncoded());
        } else {
            publicKey = (RSAPublicKey) CryptoConstants.RSA_KEY_FACTORY.generatePublic(new X509EncodedKeySpec(Files.readAllBytes(pubKF.toPath())));
            privateKey = (RSAPrivateKey) CryptoConstants.RSA_KEY_FACTORY.generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(privKF.toPath())));
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
                            clientHandler(accepted);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, "Login handler").start();
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void clientHandler(Socket socket) throws IOException {
        NetworkConnection connection = new NetworkConnection(socket, new ConnectionInput(socket.getInputStream()), new ConnectionOutput(socket.getOutputStream()));

        System.out.println("HANDLE");
        while (!socket.isClosed() && socket.isConnected()) {
            Packet<?> packet = connection.receivePacket();
            PacketType<?> type = packet.type();
            if (type == PacketTypes.CLIENT_HELLO) {
                ClientHello hello = (ClientHello) packet.data();
                if (!Objects.equals(hello.getClientVersion(), Constants.VERSION)) {
                    connection.send(new ServerAuthResponse(false, "Version mismatch!"));
                    connection.close();
                    return;
                }
                connection.send(new ServerAuthRequest());
                Packet<ClientAuth> packet1 = connection.receivePacket();
                ClientAuth auth = packet1.data();
                System.out.println(auth.getUsername());
            } else if (type == PacketTypes.CLIENT_CREATE_ACCOUNT) {
                ClientCreateAccount data = (ClientCreateAccount) packet.data();
                System.out.println(data.getUsername());
                System.out.println(data.getKey());
                connection.send(new ServerAuthResponse(true, null));
                connection.close();
                return;
            }
        }
    }
}