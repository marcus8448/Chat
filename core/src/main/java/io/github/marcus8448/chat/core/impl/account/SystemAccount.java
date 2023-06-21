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

package io.github.marcus8448.chat.core.impl.account;

import io.github.marcus8448.chat.core.api.message.MessageAuthor;

import java.security.interfaces.RSAPublicKey;

/**
 * Account that represents the server/system (not a client)
 *
 * @param publicKey the server's public key
 */
public record SystemAccount(RSAPublicKey publicKey) implements MessageAuthor {
    @Override
    public String getShortIdName() {
        return "SYSTEM";
    }

    @Override
    public String getLongIdName() {
        return "SYSTEM";
    }

    @Override
    public String getName() {
        return "SYSTEM";
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return this.publicKey;
    }
}
