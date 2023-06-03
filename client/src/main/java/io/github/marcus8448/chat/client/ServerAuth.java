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

import io.github.marcus8448.chat.client.network.ClientNetworking;
import io.github.marcus8448.chat.core.network.connection.NetworkConnection;
import io.github.marcus8448.chat.core.network.packet.ClientCreateAccount;
import io.github.marcus8448.chat.core.network.packet.Packet;
import io.github.marcus8448.chat.core.network.packet.ServerAuthResponse;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;

public class ServerAuth {
    private static final KeyStore KEY_STORE;

    static {
        try {
            KEY_STORE = KeyStore.getInstance(KeyStore.getDefaultType());
            File file = new File(System.getProperty("user.home"), "chat.jks");
            if (file.exists()) {
                KEY_STORE.load(new FileInputStream(file), "null".toCharArray());
            } else {
                KEY_STORE.load(null, null);
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void auth(InetSocketAddress address, String username, String password) {
        try {
            PublicKey publicKey = (PublicKey) KEY_STORE.getKey("chat/" + username + "_public", password.toCharArray());
            PrivateKey privateKey = (PrivateKey) KEY_STORE.getKey("chat/" + username + "_private", password.toCharArray());
            System.out.println(publicKey);
            System.out.println(privateKey);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }

    }

    public static ServerAuthResponse createAccount(InetSocketAddress address, String username, String password) {
        try {
            if (KEY_STORE.containsAlias("chat/" + username + "_public") && KEY_STORE.containsAlias("chat/" + username + "_private")) {
                throw new KeyAlreadyExistsException();
            }
        } catch (KeyStoreException ignored) {
        }
        KeyPairGenerator rsa = null;
        try {
            rsa = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        rsa.initialize(4096);
        KeyPair keyPair = rsa.generateKeyPair();


        try (NetworkConnection connect = ClientNetworking.connect(address)) {
            connect.send(new ClientCreateAccount(username, (RSAPublicKey) keyPair.getPublic()));
            Packet<ServerAuthResponse> packet = connect.receivePacket();
            if (packet.data().isSuccess()) {
                try {
                    KEY_STORE.setKeyEntry("chat/" + username + "_public", keyPair.getPublic(), password.toCharArray(), null);
                    KEY_STORE.setKeyEntry("chat/" + username + "_private", keyPair.getPrivate(), password.toCharArray(), null);
                    saveKeys();
                } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
            return packet.data();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveKeys() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        KEY_STORE.store(new FileOutputStream(new File(System.getProperty("user.home"), "chat.jks")), "null".toCharArray());
    }
}
