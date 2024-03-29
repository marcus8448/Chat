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
import io.github.marcus8448.chat.core.api.network.packet.server.AuthenticationFailure;
import io.github.marcus8448.chat.core.api.network.packet.server.AuthenticationRequest;
import io.github.marcus8448.chat.core.api.network.packet.server.AuthenticationSuccess;

import java.io.IOException;

/**
 * The client's response to the server's authentication/identity challenge
 *
 * @see AuthenticationRequest The server's challenge
 * @see AuthenticationSuccess Server response (success)
 * @see AuthenticationFailure Server response (failure)
 */
public class Authenticate implements NetworkedData {
    /**
     * The username of the user connecting
     */
    private final Identifier username;
    /**
     * The data sent in the {@link AuthenticationRequest}, but
     * encrypted with the server's key.
     */
    private final byte[] data;

    public Authenticate(Identifier username, byte[] data) {
        this.username = username;
        this.data = data;
    }

    public Authenticate(BinaryInput input) throws IOException {
        this.username = Identifier.create(input.readString());
        this.data = input.readByteArray();
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeString(this.username.getValue());
        output.writeByteArray(this.data);
    }

    public Identifier getUsername() {
        return username;
    }

    public byte[] getData() {
        return data;
    }
}
