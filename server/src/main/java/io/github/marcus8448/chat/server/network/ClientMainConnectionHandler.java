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

import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.PacketTypes;
import io.github.marcus8448.chat.core.network.packet.Packet;
import io.github.marcus8448.chat.core.network.packet.SendMessage;
import io.github.marcus8448.chat.core.user.User;
import io.github.marcus8448.chat.server.Server;
import io.github.marcus8448.chat.server.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClientMainConnectionHandler implements ClientConnectionHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Server server;
    private final PacketPipeline pipeline;
    private final User user;

    public ClientMainConnectionHandler(Server server, PacketPipeline pipeline, User user) {
        this.server = server;
        this.pipeline = pipeline;
        this.user = user;
    }

    @Override
    public <Data extends NetworkedData> void handle(Packet<Data> packet) {
        PacketType<? extends Data> type = packet.type();
        if (type == PacketTypes.SEND_MESSAGE) {
            long time = Utils.currentTimeNonDecreasing();
            SendMessage send = ((SendMessage) packet.data());
            this.server.executor.submit(() -> this.server.receiveMessage(time, this.user, send.getChecksum(), send.getMessage()));
        }
    }

    @Override
    public void run() {
        try {
            while (this.pipeline.isOpen()) {
                this.handle(this.pipeline.receivePacket());
            }
        } catch (Exception e) {
            LOGGER.error("Error in client communications - connection closed.", e);
        }
        this.shutdown();
    }

    @Override
    public void shutdown() {
        try {
            this.pipeline.close();
            this.server.executor.execute(() -> this.server.disconnect(this, this.user));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <Data extends NetworkedData> void send(PacketType<Data> type, Data data) {
        try {
            this.pipeline.send(type, data);
        } catch (IOException e) {
            LOGGER.error("Failed to send packet", e);
        }
    }
}
