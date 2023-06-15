package io.github.marcus8448.chat.server.network;

import io.github.marcus8448.chat.core.api.network.PacketHandler;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.PacketTypes;
import io.github.marcus8448.chat.core.network.packet.Packet;
import io.github.marcus8448.chat.server.Server;
import io.github.marcus8448.chat.server.util.Utils;

public interface ClientConnectionHandler extends PacketHandler, Runnable {
    void shutdown();

    <Data extends NetworkedData> void send(Packet<Data> packet);
}
