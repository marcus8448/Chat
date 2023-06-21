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
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a type of packet. Every packet gets a unique ID
 *
 * @param <Data> the type of data contained
 */
public final class PacketType<Data extends NetworkedData> {
    /**
     * List of all available packet types
     */
    private static final List<PacketType<? extends NetworkedData>> TYPES = new ArrayList<>();
    /**
     * The next available packet id
     */
    private static int index = 0;

    static {
        // register types
        ServerPacketTypes.initialize();
        ClientPacketTypes.initialize();
    }

    /**
     * The packet's ID
     */
    private final int id;
    /**
     * The type contained in this packet
     */
    private final Class<Data> clazz;
    /**
     * Deserializes the data from a network input stream
     */
    private final DataDeserializer<Data> deserializer;

    private PacketType(int id, Class<Data> clazz, DataDeserializer<Data> deserializer) {
        this.id = id;
        this.clazz = clazz;
        this.deserializer = deserializer;
    }

    /**
     * Creates a new packet type
     *
     * @param clazz        the class of the data type
     * @param deserializer the data deserializer
     * @param <Data>       the type of data
     * @return a new packet type
     */
    @Contract("_, _ -> new")
    public static <Data extends NetworkedData> PacketType<Data> create(Class<Data> clazz, DataDeserializer<Data> deserializer) {
        PacketType<Data> value = new PacketType<>(index++, clazz, deserializer); // create the type
        TYPES.add(value); // add it to the registry
        return value;
    }

    /**
     * Get the packet type registered to the given id (or null if it does not exist)
     *
     * @param id the registry index
     * @return the packet at the associated id
     */
    public static PacketType<? extends NetworkedData> getType(int id) {
        return TYPES.get(id);
    }

    /**
     * @return the class that contains the packet's data
     */
    public Class<Data> getDataClass() {
        return clazz;
    }

    /**
     * @return the id of the packet
     */
    public int getId() {
        return this.id;
    }

    /**
     * Deserializes packet data from the given input
     *
     * @param input the raw data to deserialize
     * @return the deserialized data
     */
    public Data create(BinaryInput input) throws IOException {
        return this.deserializer.readFromNetwork(input);
    }

    @Override
    public String toString() {
        return "PacketType: " + this.clazz.getSimpleName();
    }

    /**
     * Deserializes packet data from a binary stream
     *
     * @param <D> the data type to deserialize
     */
    @FunctionalInterface
    public interface DataDeserializer<D extends NetworkedData> {
        /**
         * Deserializes packet data from a binary stream
         *
         * @param input the source to deserialize from
         * @return a new data instance
         */
        D readFromNetwork(BinaryInput input) throws IOException;
    }
}
