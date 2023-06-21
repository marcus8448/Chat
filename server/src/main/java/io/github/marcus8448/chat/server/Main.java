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
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) throws IOException, InvalidKeySpecException {
        LOGGER.info("Loading Chat Server v{}", Constants.VERSION);
        LOGGER.info("Running on Java {} ({} version {} from {})", System.getProperty("java.version"), System.getProperty("java.vm.name"), System.getProperty("java.vm.version"), System.getProperty("java.specification.vendor"));
        File privateKeyFile = new File("server.key");
        RSAPublicKey publicKey;
        RSAPrivateCrtKey privateKey;
        if (!privateKeyFile.exists()) {
            LOGGER.warn("No existing server keypair found. Generating...");
            KeyPair keyPair = CryptoHelper.RSA_KEY_GENERATOR.generateKeyPair();
            privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
            publicKey = (RSAPublicKey) keyPair.getPublic();
            LOGGER.info("Server keypair successfully generated");
            Files.write(privateKeyFile.toPath(), privateKey.getEncoded());
        } else {
            privateKey = CryptoHelper.decodeRsaPrivateKey(Files.readAllBytes(privateKeyFile.toPath()));
            publicKey = (RSAPublicKey) CryptoHelper.RSA_KEY_FACTORY.generatePublic(new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent(), privateKey.getParams()));
            LOGGER.info("Server keypair loaded successfully");
        }
        LOGGER.info("Identity: {}", CryptoHelper.sha256Hash(publicKey.getEncoded()));
        try (Server server = new Server(Constants.PORT, publicKey, privateKey)) {
            server.launch();
        }
    }
}