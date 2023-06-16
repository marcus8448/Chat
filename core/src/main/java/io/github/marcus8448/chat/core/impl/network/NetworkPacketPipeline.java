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

package io.github.marcus8448.chat.core.impl.network;

import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.network.connection.BinaryOutput;
import io.github.marcus8448.chat.core.impl.network.connection.EncryptedNetworkPipeline;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.packet.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;

public class NetworkPacketPipeline implements PacketPipeline {
    private static final Logger LOGGER = LogManager.getLogger();

    private final int packetHeader;
    private final Socket socket;
    private final BinaryInput input;
    private final BinaryOutput output;

    public NetworkPacketPipeline(int packetHeader, @NotNull Socket socket, @NotNull BinaryInput input, @NotNull BinaryOutput output) throws IOException {
        this.packetHeader = packetHeader;
        this.socket = socket;
        this.input = input;
        this.output = output;
    }

    @Override
    public @NotNull PacketPipeline encryptWith(@NotNull SecretKey secretKey) throws IOException {
        return new EncryptedNetworkPipeline(this.packetHeader, this.socket, this.input, this.output, secretKey);
    }

    @Override
    public synchronized <Data extends NetworkedData> void send(PacketType<Data> type, Data networkedData) throws IOException {
        LOGGER.debug("Sending packet {}", type.getDataClass().getName());
        this.output.writeInt(this.packetHeader);
        this.output.writeShort(type.getId());
        networkedData.write(this.output);
    }

    @Override
    public <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException {
        this.input.seekToIdentifier(this.packetHeader);
        PacketType<Data> type = (PacketType<Data>) PacketType.getType(this.input.readShort());
        LOGGER.debug("Received packet {}", type.getDataClass().getName());
        return new Packet<>(type, type.create(this.input));
    }

    @Override
    public <Data extends NetworkedData> Packet<Data> receivePacket(PacketType<Data> type) throws IOException {
        Packet<NetworkedData> packet = this.receivePacket();
        if (packet.type() != type) {
            LOGGER.debug("Discarding packet {}", type.getDataClass().getName());
            return this.receivePacket(type);
        }
        LOGGER.debug("Received packet {}", type.getDataClass().getName());
        return (Packet<Data>) packet;
    }

    @Override
    public void close() throws IOException {
        this.input.close();
        this.output.close();
    }

    @Override
    public boolean isOpen() {
        return !this.socket.isClosed();
    }
}
