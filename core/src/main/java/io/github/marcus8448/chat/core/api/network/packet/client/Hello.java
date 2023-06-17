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

package io.github.marcus8448.chat.core.api.network.packet.client;

import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;
import io.github.marcus8448.chat.core.api.network.packet.server.AuthenticationRequest;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * First packet sent on client connection with a server
 * Initial part of connection handshake - nothing is encrypted yet
 * @see AuthenticationRequest The server's (expected) response
 */
public class Hello implements NetworkedData {
    /**
     * The client brand
     */
    private final String brand;
    /**
     * The client's version
     */
    private final String version;
    /**
     * The RSA public key of the client
     */
    private final RSAPublicKey key;

    public Hello(String brand, String version, RSAPublicKey key) {
        this.brand = brand;
        this.version = version;
        this.key = key;
    }

    public Hello(BinaryInput input) throws IOException {
        this.brand = input.readString();
        this.version = input.readString();
        try {
            this.key = CryptoHelper.decodeRsaPublicKey(input.readByteArray());
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeString(this.brand);
        output.writeString(this.version);
        output.writeByteArray(this.key.getEncoded());
    }

    public RSAPublicKey getKey() {
        return key;
    }

    public String getBrand() {
        return brand;
    }

    public String getVersion() {
        return version;
    }
}
