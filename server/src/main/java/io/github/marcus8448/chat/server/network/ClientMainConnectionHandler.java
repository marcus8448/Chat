package io.github.marcus8448.chat.server.network;

import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.message.Message;
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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
            LOGGER.error("Error in client communications", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.pipeline.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <Data extends NetworkedData> void send(Packet<Data> packet) {
        try {
            this.pipeline.send(packet.type(), packet.data());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
