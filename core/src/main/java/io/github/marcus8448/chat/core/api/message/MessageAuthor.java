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

package io.github.marcus8448.chat.core.api.message;

import io.github.marcus8448.chat.core.impl.account.SystemAccount;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.security.interfaces.RSAPublicKey;

/**
 * Represents the source of a message
 *
 * @see io.github.marcus8448.chat.core.api.account.User
 */
public interface MessageAuthor {
    /**
     * Creates a new system (server) author type
     *
     * @param key the public key of the server
     * @return an author that represents the server the client is connected to
     */
    @Contract("_ -> new")
    static @NotNull MessageAuthor system(RSAPublicKey key) {
        return new SystemAccount(key);
    }

    /**
     * @return a formatted string containing the name and truncated id of the author
     */
    String getShortIdName();

    /**
     * @return a formatted string containing the name and full id of the author
     */
    String getLongIdName();

    /**
     * @return the author's username
     */
    String getName();

    /**
     * @return the unique public key representing the author
     */
    RSAPublicKey getPublicKey();
}
