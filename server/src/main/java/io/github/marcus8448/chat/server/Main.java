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
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
        try (Server server = new Server(publicKey, privateKey)) {
            server.launch(Constants.PORT);
        }
    }
}