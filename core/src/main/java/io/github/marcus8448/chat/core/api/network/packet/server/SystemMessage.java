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

import java.io.IOException;

/**
 * Represents a message sent by the server (not a normal client)
 */
public class SystemMessage implements NetworkedData {
    private final Identifier channel;
    /**
     * When the server sent the message
     */
    private final long timestamp;
    /**
     * The text contents of the server's message
     */
    private final String contents;
    /**
     * The signature, created with the server's RSA key that verifies the message's authenticity
     */
    private final byte[] signature;

    public SystemMessage(Identifier channel, long timestamp, String contents, byte[] signature) {
        this.channel = channel;
        this.timestamp = timestamp;
        this.contents = contents;
        this.signature = signature;
    }

    public SystemMessage(BinaryInput input) throws IOException {
        this.channel = input.readIdentifier();
        this.timestamp = input.readLong();
        this.contents = input.readString();
        this.signature = input.readByteArray();
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeIdentifier(this.channel);
        output.writeLong(this.timestamp);
        output.writeString(this.contents);
        output.writeByteArray(this.signature);
    }

    public Identifier getChannel() {
        return channel;
    }

    public byte[] getSignature() {
        return signature;
    }

    public String getContents() {
        return contents;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
