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

package io.github.marcus8448.chat.core.network.packet;

import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.connection.ConnectionInput;
import io.github.marcus8448.chat.core.network.connection.ConnectionOutput;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

public class ClientAuth implements NetworkedData {
    private String username;
    private byte[] data;

    public ClientAuth(String username, byte[] data) {
        this.username = username;
        this.data = data;
    }

    public ClientAuth() {}

    @Override
    public void write(ConnectionOutput output) throws IOException {
        output.writeString(this.username);
        output.writeShort(data.length);
        output.write(data);
    }

    @Override
    public void read(ConnectionInput input) throws IOException {
        this.username = input.readString();
        this.data = input.readNBytes(input.readShort());
    }

    public String getUsername() {
        return username;
    }

    public byte[] getData() {
        return data;
    }
}
