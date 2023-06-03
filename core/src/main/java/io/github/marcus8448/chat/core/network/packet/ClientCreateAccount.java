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

import io.github.marcus8448.chat.core.crypto.RSAConstants;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.connection.ConnectionInput;
import io.github.marcus8448.chat.core.network.connection.ConnectionOutput;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class ClientCreateAccount implements NetworkedData {
    private RSAPublicKey key;
    private String username;

    public ClientCreateAccount() {}

    public ClientCreateAccount(String username, RSAPublicKey key) {
        this.username = username;
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public RSAPublicKey getKey() {
        return key;
    }

    @Override
    public void write(ConnectionOutput output) throws IOException {
        output.writeString(this.username);
        byte[] encoded = this.key.getEncoded();
        output.writeShort(encoded.length);
        output.write(encoded);
    }

    @Override
    public void read(ConnectionInput input) throws IOException {
        this.username = input.readString();
        int len = input.readShort();
        byte[] encodedKey = input.readNBytes(len);
        try {
            this.key = (RSAPublicKey) RSAConstants.KEY_FACTORY.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
