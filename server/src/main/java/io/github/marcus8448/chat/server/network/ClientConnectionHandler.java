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

package io.github.marcus8448.chat.server.network;

import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.PacketType;
import org.jetbrains.annotations.Nullable;

public interface ClientConnectionHandler extends Runnable {
    /**
     * Closes the connection with the client
     */
    void shutdown();

    /**
     * Acts on received packets
     *
     * @param packet the packet received
     * @param <Data> the type of packet received
     */
    <Data extends NetworkedData> void handle(Packet<Data> packet);

    /**
     * Sends a packet to the connected client
     *
     * @param type   the type of packet to send
     * @param data   the packet body
     * @param <Data> the type of packet body
     */
    <Data extends NetworkedData> void send(PacketType<Data> type, Data data);

    /**
     * @return the user associated with this client
     */
    @Nullable User getUser();
}
