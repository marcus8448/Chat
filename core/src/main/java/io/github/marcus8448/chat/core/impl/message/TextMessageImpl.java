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

package io.github.marcus8448.chat.core.impl.message;

import io.github.marcus8448.chat.core.api.message.MessageAuthor;
import io.github.marcus8448.chat.core.api.message.TextMessage;

import java.nio.charset.StandardCharsets;

public record TextMessageImpl(long timestamp, MessageAuthor author, String contents, byte[] signature) implements TextMessage {
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public MessageAuthor getAuthor() {
        return this.author;
    }

    @Override
    public byte[] getContents() {
        return this.contents.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getSignature() {
        return this.signature;
    }

    @Override
    public String getMessage() {
        return this.contents;
    }
}
