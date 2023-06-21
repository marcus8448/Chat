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

package io.github.marcus8448.chat.core.api.network.packet;

import io.github.marcus8448.chat.core.api.network.NetworkedData;

/**
 * Represents a fully-formed packet: type/header + data
 *
 * @param type   the type of packet
 * @param data   the body of the packet
 * @param <Data> the class type of the data
 */
public record Packet<Data extends NetworkedData>(PacketType<Data> type, Data data) {
    /**
     * Attempts to coerce the packet data to the desired type
     *
     * @param type       the desired type of packet
     * @param <RealData> the requested data type
     * @return the data contained in this packet, cast to the new type
     * @throws UnsupportedOperationException if the type does not match
     */
    public <RealData extends NetworkedData> RealData getAs(PacketType<RealData> type) {
        if (this.type() != type) throw new UnsupportedOperationException();
        return (RealData) this.data();
    }
}
