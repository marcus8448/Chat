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

import io.github.marcus8448.chat.core.api.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.connection.BinaryOutput;
import io.github.marcus8448.chat.core.api.crypto.CryptoConstants;
import io.github.marcus8448.chat.core.network.NetworkedData;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class ServerAuthRequest implements NetworkedData {
    private final RSAPublicKey key;
    private final byte[] authData;

    public ServerAuthRequest(RSAPublicKey key, byte[] authData) {
        this.key = key;
        this.authData = authData;
    }

    public ServerAuthRequest(BinaryInput input) throws IOException {
        try {
            this.key = (RSAPublicKey) CryptoConstants.RSA_KEY_FACTORY.generatePublic(new PKCS8EncodedKeySpec(input.readByteArray()));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        this.authData = input.readByteArray();
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeByteArray(this.key.getEncoded());
        output.writeByteArray(this.authData);
    }

    public RSAPublicKey getKey() {
        return key;
    }

    public byte[] getAuthData() {
        return authData;
    }
}
