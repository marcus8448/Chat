package io.github.marcus8448.chat.core.api.network;

import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.packet.Packet;

public interface PacketHandler {
    <Data extends NetworkedData> void handle(Packet<Data> packet);
}
