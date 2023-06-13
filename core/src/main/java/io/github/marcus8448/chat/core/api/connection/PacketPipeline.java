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

package io.github.marcus8448.chat.core.api.connection;

import io.github.marcus8448.chat.core.impl.connection.BasicNetworkPipeline;
import io.github.marcus8448.chat.core.impl.connection.EncryptedNetworkPipeline;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.packet.Packet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public interface PacketPipeline {
    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull PacketPipeline createBasic(int header, @NotNull BinaryInput input, @NotNull BinaryOutput output) {
        try {
            return new BasicNetworkPipeline(header, input, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    static @NotNull PacketPipeline createEncrypted(int header, @NotNull BinaryInput input, @NotNull BinaryOutput output, @NotNull RSAPublicKey sendingKey, @NotNull RSAPrivateKey receivingKey) {
        try {
            return new EncryptedNetworkPipeline(header, input, output, sendingKey, receivingKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    <Data extends NetworkedData> void send(PacketType<Data> type, Data networkedData) throws IOException;

    <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException;

    <Data extends NetworkedData> Packet<Data> receivePacket(PacketType<Data> type) throws IOException;
}
