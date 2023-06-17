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
import org.jetbrains.annotations.Nullable;

import java.security.interfaces.RSAPublicKey;

public record User(int sessionId, String username, RSAPublicKey key, byte @Nullable [] base64Icon) implements MessageAuthor {
    @Override
    public String getFormattedName() {
        String hash = CryptoHelper.sha256Hash(this.key.getEncoded());
        return this.username() + " [" + hash.substring(0, hash.length() / 2) + "]";
    }

    @Override
    public String getName() {
        return this.username;
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return this.key;
    }
}
