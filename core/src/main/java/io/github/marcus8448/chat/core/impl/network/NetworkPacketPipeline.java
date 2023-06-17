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

import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.PacketType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;

/**
 * A packet pipeline backed by a network connection.
 * Is not encrypted and thereby insecure.
 * Used for initial handshake (before encrypting)
 * @see EncryptedNetworkPipeline
 */
public class NetworkPacketPipeline implements PacketPipeline {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The packet header in use
     */
    private final int packetHeader;
    /**
     * The backing network connection (socket)
     */
    private final Socket socket;
    /**
     * The socket's input stream, conveniently wrapped in a binary input
     */
    private final BinaryInput input;
    /**
     * The socket's input stream, conveniently wrapped in a binary output
     */
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
        this.output.writeInt(this.packetHeader); // write the packet header
        this.output.writeShort(type.getId()); // write the packet id
        networkedData.write(this.output); // write the raw data - no length knowledge required.
    }

    @Override
    public <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException {
        this.input.seekToIdentifier(this.packetHeader); // wait for a packet header
        PacketType<Data> type = (PacketType<Data>) PacketType.getType(this.input.readShort()); // get the packet type
        LOGGER.debug("Received packet {}", type.getDataClass().getName());
        return new Packet<>(type, type.create(this.input)); // read the data and create a packet
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
