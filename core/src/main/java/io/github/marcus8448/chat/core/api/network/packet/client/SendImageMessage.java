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
import io.github.marcus8448.chat.core.api.network.packet.server.AddMessage;

import java.io.IOException;

/**
 * Represents a new message sent from the client -> server
 *
 * @see AddMessage The server's expected response to ALL clients
 */
public class SendImageMessage implements NetworkedData {
    private final Identifier channel;
    private final int width;
    private final int height;
    /**
     * The contents of the message
     */
    private final int[] message;
    /**
     * The checksum verifying the contents of the message
     */
    private final byte[] signature;

    public SendImageMessage(BinaryInput input) throws IOException {
        this.channel = input.readIdentifier();
        this.width = input.readShort();
        this.height = input.readShort();
        this.message = input.readIntArray(this.width * this.height);
        this.signature = input.readByteArray();
    }

    public SendImageMessage(Identifier channel, int width, int height, int[] message, byte[] signature) {
        this.channel = channel;
        this.width = width;
        this.height = height;
        this.message = message;
        this.signature = signature;
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeIdentifier(this.channel);
        output.writeShort(this.width);
        output.writeShort(this.height);
        output.writeIntArray(this.width * this.height, this.message);
        output.writeByteArray(this.signature);
    }

    public Identifier getChannel() {
        return channel;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getImage() {
        return message;
    }

    public byte[] getSignature() {
        return signature;
    }
}
