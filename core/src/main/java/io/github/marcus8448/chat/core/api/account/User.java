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

package io.github.marcus8448.chat.core.api.account;

import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.message.MessageAuthor;
import io.github.marcus8448.chat.core.api.misc.Identifier;
import org.jetbrains.annotations.Nullable;

import java.security.interfaces.RSAPublicKey;

/**
 * A client connected to a chat server
 *
 * @param sessionId the unique integer id identifying this client - changes upon disconnect/reconnect
 * @param username  the username of the user - can be changed at anytime
 * @param key       the public key (RSA) of the user - the permanent ID of the user
 * @param icon      the user's profile picture (NYI)
 */
public record User(int sessionId, Identifier username, RSAPublicKey key,
                   byte @Nullable [] icon) implements MessageAuthor {
    @Override
    public String getShortIdName() {
        String hash = CryptoHelper.sha256Hash(this.key.getEncoded());
        return this.username() + " [" + hash.substring(0, 32) + "]";
    }

    @Override
    public String getLongIdName() {
        String hash = CryptoHelper.sha256Hash(this.key.getEncoded());
        return this.username() + " [" + hash + "]";
    }

    @Override
    public String getName() {
        return this.username.getValue();
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return this.key;
    }
}
