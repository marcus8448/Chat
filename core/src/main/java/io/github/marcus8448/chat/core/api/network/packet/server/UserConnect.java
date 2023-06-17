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

import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

public class UserConnect implements NetworkedData {
    private final User user;

    public UserConnect(User user) {
        this.user = user;
    }

    public UserConnect(BinaryInput input) throws IOException {
        int sessionId = input.readInt();
        RSAPublicKey key = null;
        try {
            key = CryptoHelper.decodeRsaPublicKey(input.readByteArray());
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        String username = input.readString();
        this.user = new User(sessionId, username, key, null);
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeInt(user.sessionId());
        output.writeByteArray(user.key().getEncoded());
        output.writeString(user.username());
    }

    public User getUser() {
        return user;
    }
}
