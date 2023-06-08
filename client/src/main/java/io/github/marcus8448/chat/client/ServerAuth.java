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

import io.github.marcus8448.chat.client.network.AuthenticationData;
import io.github.marcus8448.chat.client.network.ClientNetworking;
import io.github.marcus8448.chat.core.Result;
import io.github.marcus8448.chat.core.api.Constants;
import io.github.marcus8448.chat.core.api.connection.PacketPipeline;
import io.github.marcus8448.chat.core.api.crypto.CryptoConstants;
import io.github.marcus8448.chat.core.network.PacketTypes;
import io.github.marcus8448.chat.core.network.packet.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class ServerAuth {
    public static Result<AuthenticationData, String> auth(InetSocketAddress address, String username, String password) {
        RSAPrivateCrtKey privateKey;
        try {
            Result<RSAPrivateCrtKey, String> result = loadKey(username, password.toCharArray());
            if (result.isError()) return result.coerceError();
            privateKey = result.unwrap();

            PublicKey publicKey = CryptoConstants.RSA_KEY_FACTORY.generatePublic(new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent(), privateKey.getParams()));
            if (publicKey == null) {
                return Result.error("Account credentials does not exist");
            }
        } catch (InvalidKeySpecException | IOException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            return Result.error("Invalid credentials");
        }

        boolean noClose = false;
        PacketPipeline connect = null;
        try {
            connect = ClientNetworking.connect(address);
            connect.send(PacketTypes.CLIENT_HELLO, new ClientHello(Constants.VERSION, Constants.VERSION));

            Packet<ServerAuthRequest> packet = connect.receivePacket();
            Cipher cipher = CryptoConstants.getRsaCipher();
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] bytes = cipher.doFinal(packet.data().getAuthData());
            cipher.init(Cipher.ENCRYPT_MODE, packet.data().getKey());
            byte[] output = cipher.doFinal(bytes);

            connect.send(PacketTypes.CLIENT_AUTH, new ClientAuth(username, output));
            Packet<ServerAuthResponse> networkedDataPacket = connect.receivePacket();
            if (networkedDataPacket.data().isSuccess()) {
                noClose = true;
                return Result.ok(new AuthenticationData(privateKey, packet.data().getKey(), connect));
            } else {
                return Result.error(networkedDataPacket.data().getFailureReason());
            }
        } catch (IOException e) {
            return Result.error("Failed to connect to server");
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            return Result.error("Encryption failure");
        } finally {
            if (!noClose && connect != null) {
                try {
                    connect.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static Result<String, String> createAccount(InetSocketAddress address, String username, String password) {
        if (new File(username + ".key").exists()) {
            return Result.error("Account already exists");
        }

        System.out.println("generating key");
        KeyPair keyPair = CryptoConstants.RSA_KEY_GENERATOR.generateKeyPair();
        System.out.println("gen done");

        try (PacketPipeline connect = ClientNetworking.connect(address)) {
            connect.send(PacketTypes.CLIENT_CREATE_ACCOUNT, new ClientCreateAccount(username, (RSAPublicKey) keyPair.getPublic()));
            Packet<ServerAuthResponse> packet = connect.receivePacket();
            if (packet.data().isSuccess()) {
                try {
                    saveKeys(username, (RSAPrivateCrtKey) keyPair.getPrivate(), password.toCharArray());
                    return Result.ok("Success.");
                } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException |
                         IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException e) {
                    throw new RuntimeException(e);
                }
            }
            return Result.error(packet.data().getFailureReason());
        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }

    private static Result<RSAPrivateCrtKey, String> loadKey(String username, char[] password) throws IOException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        File file = new File(username + ".key");
        if (!file.exists()) {
            return Result.error("Missing key for user");
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        SecretKey key = new SecretKeySpec(CryptoConstants.PBKDF2_SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(password, username.getBytes(StandardCharsets.UTF_8), 65536, 256)).getEncoded(), "AES");
        Cipher aesCipher = CryptoConstants.getAesCipher();
        aesCipher.init(Cipher.DECRYPT_MODE, key);
        return Result.ok((RSAPrivateCrtKey) CryptoConstants.RSA_KEY_FACTORY.generatePrivate(new PKCS8EncodedKeySpec(aesCipher.doFinal(bytes))));
    }

    private static void saveKeys(String username, RSAPrivateCrtKey key, char[] password) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        File file = new File(username + ".key");
        if (file.exists()) {
            file.delete();
        }
        SecretKey encode = new SecretKeySpec(CryptoConstants.PBKDF2_SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(password, username.getBytes(StandardCharsets.UTF_8), 65536, 256)).getEncoded(), "AES");
        Cipher aesCipher = CryptoConstants.getAesCipher();
        aesCipher.init(Cipher.ENCRYPT_MODE, encode);
        Files.write(file.toPath(), aesCipher.doFinal(key.getEncoded()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }
}
