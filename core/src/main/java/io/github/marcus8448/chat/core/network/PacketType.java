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

import io.github.marcus8448.chat.core.network.connection.ConnectionInput;
import io.github.marcus8448.chat.core.network.packet.ClientAuth;
import io.github.marcus8448.chat.core.network.packet.ClientCreateAccount;
import io.github.marcus8448.chat.core.network.packet.ClientHello;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PacketType<Data extends NetworkedData> {
    private static final Map<Class<? extends NetworkedData>, PacketType<? extends NetworkedData>> REGISTERED_TYPES = new HashMap<>();
    private static final List<PacketType<? extends NetworkedData>> TYPES = new ArrayList<>();

    public static final PacketType<ClientHello> CLIENT_HELLO = PacketType.create(ClientHello.class, ClientHello::new);
    public static final PacketType<ClientAuth> CLIENT_AUTH = PacketType.create(ClientAuth.class, ClientAuth::new);
    public static final PacketType<ClientCreateAccount> CLIENT_CREATE_ACCOUNT = PacketType.create(ClientCreateAccount.class, ClientCreateAccount::new);

    private static int index = 0;
    private final int id;
    private final Supplier<Data> defaultConstructor;

    public PacketType(int id, Supplier<Data> defaultConstructor) {
        this.id = id;
        this.defaultConstructor = defaultConstructor;
    }

    public static <Data extends NetworkedData> PacketType<Data> create(Class<Data> clazz, Supplier<Data> defaultConstructor) {
        PacketType<Data> value = new PacketType<>(index++, defaultConstructor);
        REGISTERED_TYPES.put(clazz, value);
        TYPES.add(value);
        return value;
    }

    public static PacketType<? extends NetworkedData> getType(NetworkedData networkedData) {
        return REGISTERED_TYPES.get(networkedData.getClass());
    }

    public static PacketType<? extends NetworkedData> getType(int id) {
        return TYPES.get(id);
    }

    public static <Data extends NetworkedData> Data read(int id, ConnectionInput input) throws IOException {
        Data serializable = (Data) getType(id).defaultConstructor.get(); // SAFE: We control the types put into the list.
        serializable.read(input);
        return serializable;
    }

    public int getId() {
        return this.id;
    }

    public Data create(ConnectionInput input) throws IOException {
        Data data = this.defaultConstructor.get();
        data.read(input);
        return data;
    }
}
