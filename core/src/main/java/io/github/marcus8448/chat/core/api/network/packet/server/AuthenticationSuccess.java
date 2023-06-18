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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuthenticationSuccess implements NetworkedData {
    private final List<User> users;

    public AuthenticationSuccess(Collection<User> users) {
        this.users = new ArrayList<>(users); //copy
    }

    public AuthenticationSuccess(BinaryInput input) throws IOException {
        int len = input.readShort();
        this.users = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            int sessionId = input.readInt();
            RSAPublicKey key = null;
            try {
                key = CryptoHelper.decodeRsaPublicKey(input.readByteArray());
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
            String username = input.readString();
//            byte[] icon = input.readByteArray();
            this.users.add(new User(sessionId, username, key, null));
        }
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeShort(this.users.size());
        for (User user : this.users) {
            output.writeInt(user.sessionId());
            output.writeByteArray(user.key().getEncoded());
            output.writeString(user.username());
//            output.writeByteArray(user.icon());
        }
    }

    public List<User> getUsers() {
        return users;
    }
}
