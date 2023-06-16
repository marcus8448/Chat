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

package io.github.marcus8448.chat.core.api.network;

import io.github.marcus8448.chat.core.api.network.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.network.connection.BinaryOutput;
import io.github.marcus8448.chat.core.impl.network.NetworkPacketPipeline;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.packet.Packet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public interface PacketPipeline extends Closeable {
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull PacketPipeline createNetwork(int header, @NotNull Socket socket) {
        try {
            return new NetworkPacketPipeline(header, socket, BinaryInput.stream(socket.getInputStream()), BinaryOutput.stream(socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull PacketPipeline encryptWith(@NotNull SecretKey secretKey) throws IOException;

    <Data extends NetworkedData> void send(PacketType<Data> type, Data networkedData) throws IOException;

    <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException;

    <Data extends NetworkedData> Packet<Data> receivePacket(PacketType<Data> type) throws IOException;

    @Override
    void close() throws IOException;

    boolean isOpen();
}
