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

import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.network.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.network.connection.BinaryOutput;
import io.github.marcus8448.chat.core.network.NetworkedData;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

public class ClientCreateAccount implements NetworkedData {
    private final RSAPublicKey key;
    private final String username;

    public ClientCreateAccount(String username, RSAPublicKey key) {
        this.username = username;
        this.key = key;
    }

    public ClientCreateAccount(BinaryInput input) throws IOException {
        this.username = input.readString();
        byte[] encodedKey = input.readByteArray();
        try {
            this.key = CryptoHelper.decodeRsaPublicKey(encodedKey);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsername() {
        return username;
    }

    public RSAPublicKey getKey() {
        return key;
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeString(this.username);
        output.writeByteArray(this.key.getEncoded());
    }
}
