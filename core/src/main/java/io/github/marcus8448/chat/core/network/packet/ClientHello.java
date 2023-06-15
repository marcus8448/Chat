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

package io.github.marcus8448.chat.core.network.packet;

import io.github.marcus8448.chat.core.api.network.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.network.connection.BinaryOutput;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.network.NetworkedData;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class ClientHello implements NetworkedData {
    private final String clientBrand;
    private final String clientVersion;
    private final RSAPublicKey key;

    public ClientHello(String clientBrand, String clientVersion, RSAPublicKey key) {
        this.clientBrand = clientBrand;
        this.clientVersion = clientVersion;
        this.key = key;
    }

    public ClientHello(BinaryInput input) throws IOException {
        this.clientBrand = input.readString();
        this.clientVersion = input.readString();
        try {
            this.key = (RSAPublicKey) CryptoHelper.RSA_KEY_FACTORY.generatePublic(new X509EncodedKeySpec(input.readByteArray()));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeString(this.clientBrand);
        output.writeString(this.clientVersion);
        output.writeByteArray(this.key.getEncoded());
    }

    public RSAPublicKey getKey() {
        return key;
    }

    public String getClientBrand() {
        return clientBrand;
    }

    public String getClientVersion() {
        return clientVersion;
    }
}
