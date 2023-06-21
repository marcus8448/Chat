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

import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.impl.message.InsecureTextMessage;
import io.github.marcus8448.chat.core.impl.message.TextMessageImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.security.Signature;

/**
 * Represents a message
 */
public interface Message {
    @ApiStatus.Internal
    ThreadLocal<Signature> RSA_SIGNATURE = ThreadLocal.withInitial(CryptoHelper::createRsaSignature);

    @Contract(value = "_, _, _, _ -> new", pure = true)
    static @NotNull TextMessage text(long timestamp, MessageAuthor author, String contents, byte[] signature) {
        return new TextMessageImpl(timestamp, author, contents, signature);
    }


    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull TextMessage unverifiedText(long timestamp, MessageAuthor author, String contents) {
        return new InsecureTextMessage(timestamp, author, contents);
    }

    @Contract(pure = true)
    long getTimestamp();

    @Contract(pure = true)
    MessageAuthor getAuthor();

    @Contract(pure = true)
    byte[] getSignature();

    /**
     * Verifies that the message checksum matches the contents (message was actually authored by the author)
     *
     * @return whether the message checksum is valid
     */
    boolean verifySignature();

    MessageType getType();
}
