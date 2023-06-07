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

package io.github.marcus8448.chat.core.network.connection;

import io.github.marcus8448.chat.core.Constants;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.packet.Packet;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class NetworkConnection implements Closeable {
    private final Socket socket;
    protected ConnectionInput input;
    protected ConnectionOutput output;

    public NetworkConnection(Socket socket, ConnectionInput input, ConnectionOutput output) {
        this.socket = socket;
        this.input = input;
        this.output = output;
    }

    public void send(NetworkedData networkedData) throws IOException {
        this.output.writeInt(Constants.PACKET_HEADER);
        this.output.writeInt(PacketType.getType(networkedData).getId());
        networkedData.write(this.output);
        this.output.flush();
    }

    public <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException {
        this.input.seekToIdentifier(Constants.PACKET_HEADER);
        int id = this.input.readInt();

        PacketType<Data> type = (PacketType<Data>) PacketType.getType(id);
        System.out.println(type);
        return new Packet<>(type, type.create(this.input));
    }

    public <Data extends NetworkedData> Packet<Data> receivePacket(Class<Data> clazz) throws IOException {
        this.input.seekToIdentifier(Constants.PACKET_HEADER);
        int id = this.input.readInt();
        PacketType<?> type = PacketType.getType(id);
        if (type.getDataClass() != clazz) {
            System.out.println("Discarding packet " + type.getDataClass() + ".");
            return receivePacket(clazz);
        }
        return new Packet<>(((PacketType<Data>) type), ((PacketType<Data>) type).create(this.input));
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
        this.input.close();
        this.output.close();
    }
}
