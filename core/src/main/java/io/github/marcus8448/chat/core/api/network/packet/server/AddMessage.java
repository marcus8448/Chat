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

import io.github.marcus8448.chat.core.api.misc.Identifier;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;
import io.github.marcus8448.chat.core.api.network.packet.client.SendMessage;

import java.io.IOException;

/**
 * Propagates a new message to the connected client(s)
 *
 * @see SendMessage
 */
public class AddMessage implements NetworkedData {
    private final Identifier channel;
    /**
     * WHen the message was received on the server
     */
    private final long timestamp;
    /**
     * The session ID of the author
     */
    private final int authorId;
    /**
     * The contents of the message
     */
    private final String contents;
    /**
     * The checksum verifying the authenticity of the message (was from the author)
     */
    private final byte[] signature;

    public AddMessage(BinaryInput input) throws IOException {
        this.channel = input.readIdentifier();
        this.timestamp = input.readLong();
        this.authorId = input.readInt();
        this.contents = input.readString();
        this.signature = input.readByteArray();
    }

    public AddMessage(Identifier channel, long timestamp, int authorId, String contents, byte[] signature) {
        this.channel = channel;
        this.timestamp = timestamp;
        this.authorId = authorId;
        this.contents = contents;
        this.signature = signature;
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeIdentifier(this.channel);
        output.writeLong(this.timestamp);
        output.writeInt(this.authorId);
        output.writeString(this.contents);
        output.writeByteArray(this.signature);
    }

    public Identifier getChannel() {
        return channel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getAuthorId() {
        return authorId;
    }

    public byte[] getSignature() {
        return signature;
    }

    public String getContents() {
        return contents;
    }
}
