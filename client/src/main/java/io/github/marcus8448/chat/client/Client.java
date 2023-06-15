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

import io.github.marcus8448.chat.client.config.AccountData;
import io.github.marcus8448.chat.client.config.Config;
import io.github.marcus8448.chat.client.ui.LoginScreen;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class Client extends Application {
    public Config config;

    public PacketPipeline connection;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private RSAPublicKey serverPubKey;

    public Client() {
        this.config = Config.load(new File("chat.json"));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        LoginScreen loginScreen = new LoginScreen(this, primaryStage);
        primaryStage.show();
    }

    public void setIdentity(PacketPipeline pipeline, RSAPublicKey serverKey, RSAPublicKey publicKey, AccountData data) {
        this.connection = pipeline;
        this.publicKey = publicKey;
        this.serverPubKey = serverKey;
        this.privateKey = data.privateKey();
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] signMessage(String contents) {
        Signature rsaSignature = CryptoHelper.createRsaSignature();
        try {
            rsaSignature.initSign(this.privateKey);
            rsaSignature.update(contents.getBytes(StandardCharsets.UTF_8));
            return rsaSignature.sign();
        } catch (InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
