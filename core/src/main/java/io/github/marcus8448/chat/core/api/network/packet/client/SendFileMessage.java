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

package io.github.marcus8448.chat.core.api.network.packet.client;

import io.github.marcus8448.chat.core.api.misc.Identifier;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;
import io.github.marcus8448.chat.core.api.network.packet.server.AddFileMessage;

import java.io.IOException;

/**
 * Represents a new file message sent from the client -> server
 *
 * @see AddFileMessage The server's expected response to ALL clients
 */
public class SendFileMessage implements NetworkedData {
    private final Identifier channel;
    /**
     * The contents of the message
     */
    private final byte[] contents;
    /**
     * The checksum verifying the contents of the message
     */
    private final byte[] signature;

    public SendFileMessage(BinaryInput input) throws IOException {
        this.channel = input.readIdentifier();
        this.contents = input.readByteArray(input.readInt());
        this.signature = input.readByteArray();
    }

    public SendFileMessage(Identifier channel, byte[] contents, byte[] signature) {
        this.channel = channel;
        this.contents = contents;
        this.signature = signature;
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeIdentifier(this.channel);
        output.writeInt(this.contents.length);
        output.writeByteArray(this.contents.length, this.contents);
        output.writeByteArray(this.signature);
    }

    public Identifier getChannel() {
        return channel;
    }

    public byte[] getContents() {
        return this.contents;
    }

    public byte[] getSignature() {
        return signature;
    }
}
