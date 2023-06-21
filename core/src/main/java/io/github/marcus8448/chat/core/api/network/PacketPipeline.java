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

import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.PacketType;
import io.github.marcus8448.chat.core.impl.network.NetworkPacketPipeline;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * Represents a connection to some other device that exchanges packets (The way the packets are transmitted is opaque)
 */
public interface PacketPipeline extends Closeable {
    /**
     * Creates a new packet pipeline backed by a socket connection
     *
     * @param header the packet header to use
     * @param socket the backing socket
     * @return a new packet pipeline
     */
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull PacketPipeline createNetwork(int header, @NotNull Socket socket) {
        try {
            return new NetworkPacketPipeline(header, socket, BinaryInput.stream(socket.getInputStream()), BinaryOutput.stream(socket.getOutputStream()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Encrypts this packet pipeline with the given AES key
     * Does not support recursive encryption
     * (e.g. a.encryptWith(x).encryptWith(y) is the same as a.encryptWith(y))
     *
     * @param secretKey the AES key to use (other formats not supported)
     * @return the encrypted pipeline
     */
    @NotNull PacketPipeline encryptWith(@NotNull SecretKey secretKey) throws IOException;

    /**
     * Sends a packet through the pipeline
     *
     * @param type          the type of packet to send
     * @param networkedData the packet's contents
     * @param <Data>        the type of packet
     */
    <Data extends NetworkedData> void send(PacketType<Data> type, Data networkedData) throws IOException;

    /**
     * Blocks until a packet is received
     *
     * @param <Data> the type of packet received
     */
    <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException;

    /**
     * Blocks until a packet of the given type is received.
     * If a packet not of this type is received, it is discarded.
     *
     * @param type   the type of packet to search for
     * @param <Data> the type of packet
     * @return the received packet
     */
    default <Data extends NetworkedData> Packet<Data> receivePacket(PacketType<Data> type) throws IOException {
        // receive any type of packet (as per usual)
        Packet<NetworkedData> packet = this.receivePacket();
        // check if it is of the desired type
        if (packet.type() != type) {
            // it is of some other type, so let's wait for another packet
            return this.receivePacket(type);
        }
        // the packet is of the right type, so return it
        return (Packet<Data>) packet;
    }

    @Override
    void close() throws IOException;

    /**
     * @return whether the pipeline is open (can send/receive packets)
     */
    boolean isOpen();
}
