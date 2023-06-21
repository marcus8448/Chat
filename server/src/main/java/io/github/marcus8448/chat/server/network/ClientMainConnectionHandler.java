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
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.packet.ClientPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.PacketType;
import io.github.marcus8448.chat.core.api.network.packet.client.SendMessage;
import io.github.marcus8448.chat.server.Server;
import io.github.marcus8448.chat.server.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
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
        if (type == ClientPacketTypes.SEND_MESSAGE) {
            long time = System.currentTimeMillis();
            SendMessage send = packet.getAs(ClientPacketTypes.SEND_MESSAGE);
            this.server.executor.submit(() -> this.server.receiveMessage(time, this.user, send.getSignature(), send.getMessage()));
        }
    }

    @Override
    public void run() {
        try {
            while (!this.server.shutdown && this.pipeline.isOpen()) {
                this.handle(this.pipeline.receivePacket());
            }
        } catch (EOFException e) {
            LOGGER.info("User " + this.user.getLongIdName() + " disconnected.");
        } catch (Exception e) {
            if (!this.server.shutdown) LOGGER.error("Error in client communications - connection closed.", e);
        }
        this.shutdown();
    }

    @Override
    public void shutdown() {
        this.server.executor.execute(() -> this.server.disconnect(this, this.user));
        try {
            this.pipeline.close();
        } catch (IOException ignored) {
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
