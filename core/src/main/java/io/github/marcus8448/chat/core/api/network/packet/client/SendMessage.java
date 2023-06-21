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
public class SendMessage implements NetworkedData {
    /**
     * The contents of the message
     */
    private final String message;
    /**
     * The checksum verifying the contents of the message
     */
    private final byte[] signature;

    public SendMessage(BinaryInput input) throws IOException {
        this.message = input.readString();
        this.signature = input.readByteArray();
    }

    public SendMessage(String message, byte[] signature) {
        this.message = message;
        this.signature = signature;
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeString(this.message);
        output.writeByteArray(this.signature);
    }

    public String getMessage() {
        return message;
    }

    public byte[] getSignature() {
        return signature;
    }
}
