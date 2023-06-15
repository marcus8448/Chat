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

package io.github.marcus8448.chat.core.network;

import io.github.marcus8448.chat.core.api.network.connection.BinaryInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketType<Data extends NetworkedData> {
    private static final List<PacketType<? extends NetworkedData>> TYPES = new ArrayList<>();

    static {
        PacketTypes.initialize();
    }
    private static int index = 0;
    private final int id;
    private final Class<Data> clazz;
    private final DataDeserializer<Data> deserializer;

    public PacketType(int id, Class<Data> clazz, DataDeserializer<Data> deserializer) {
        this.id = id;
        this.clazz = clazz;
        this.deserializer = deserializer;
    }

    public static <Data extends NetworkedData> PacketType<Data> create(Class<Data> clazz, DataDeserializer<Data> deserializer) {
        PacketType<Data> value = new PacketType<>(index++, clazz, deserializer);
        TYPES.add(value);
        return value;
    }

    public static PacketType<? extends NetworkedData> getType(int id) {
        return TYPES.get(id);
    }

    public Class<Data> getDataClass() {
        return clazz;
    }

    public int getId() {
        return this.id;
    }

    public Data create(BinaryInput input) throws IOException {
        return this.deserializer.readFromNetwork(input);
    }

    @Override
    public String toString() {
        return "PacketType: " + this.clazz.getSimpleName();
    }

    @FunctionalInterface
    public interface DataDeserializer<D extends NetworkedData> {
        D readFromNetwork(BinaryInput input) throws IOException;
    }
}
