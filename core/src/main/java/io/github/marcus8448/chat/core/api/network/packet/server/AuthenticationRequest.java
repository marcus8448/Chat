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

package io.github.marcus8448.chat.core.api.network.packet.server;

import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

public class AuthenticationRequest implements NetworkedData {
    /**
     * The server's public key
     */
    private final RSAPublicKey key;
    /**
     * The encoded AES key for future communications
     */
    private final byte[] authData;

    public AuthenticationRequest(RSAPublicKey key, byte[] authData) {
        this.key = key;
        this.authData = authData;
    }

    public AuthenticationRequest(BinaryInput input) throws IOException {
        try {
            this.key = CryptoHelper.decodeRsaPublicKey(input.readByteArray());
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

    public RSAPublicKey getServerKey() {
        return key;
    }

    public byte[] getAuthData() {
        return authData;
    }
}
