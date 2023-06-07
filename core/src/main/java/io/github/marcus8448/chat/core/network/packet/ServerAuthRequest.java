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

import io.github.marcus8448.chat.core.crypto.CryptoConstants;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.connection.ConnectionInput;
import io.github.marcus8448.chat.core.network.connection.ConnectionOutput;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class ServerAuthRequest implements NetworkedData {
    private RSAPublicKey key;
    private byte[] authData;

    public ServerAuthRequest(RSAPublicKey key, byte[] authData) {
        this.key = key;
        this.authData = authData;
    }

    public ServerAuthRequest() {
    }

    @Override
    public void write(ConnectionOutput output) throws IOException {
        byte[] encoded = this.key.getEncoded();
        output.writeShort(encoded.length);
        output.write(encoded);
        output.write(this.authData.length);
        output.write(this.authData);
    }

    @Override
    public void read(ConnectionInput input) throws IOException {
        int len = input.readShort();
        try {
            this.key = (RSAPublicKey) CryptoConstants.RSA_KEY_FACTORY.generatePublic(new PKCS8EncodedKeySpec(input.readNBytes(len)));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        len = input.readShort();
        this.authData = input.readNBytes(len);
    }

    public RSAPublicKey getKey() {
        return key;
    }

    public byte[] getAuthData() {
        return authData;
    }
}
