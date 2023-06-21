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

package io.github.marcus8448.chat.core.impl.network;

import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A packet pipeline that connects to an in-memory client and server
 */
public class LocalPipeline implements PacketPipeline {
    /**
     * Packets waiting to be handled
     */
    private final List<Packet<?>> pending = new ArrayList<>();
    /**
     * Lock to solve concurrency issues
     */
    private final Lock lock = new ReentrantLock();
    /**
     * Marker for when a packet is available
     */
    private final Condition condition = lock.newCondition();
    /**
     * The connected client/server
     */
    private LocalPipeline peer = null;

    public LocalPipeline() {
    }

    public void setPeer(@NotNull LocalPipeline peer) {
        if (this.peer != null) throw new UnsupportedOperationException();
        this.peer = peer;
    }

    private <Data extends NetworkedData> void receive(Packet<Data> packet) {
        this.lock.lock(); // claim lock on the pending list
        this.pending.add(packet); // add the packet
        this.condition.signal(); // signal that there are packets available
        this.lock.unlock(); // unlock
    }

    @Override
    public @NotNull PacketPipeline encryptWith(@NotNull SecretKey secretKey) throws IOException {
        return this; // encryption is pointless.
    }

    @Override
    public <Data extends NetworkedData> void send(PacketType<Data> type, Data networkedData) throws IOException {
        this.peer.receive(new Packet<>(type, networkedData));
    }

    @Override
    public <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException {
        if (this.pending.isEmpty()) { // check if there are available packets
            this.lock.lock(); // no packets, so claim the lock on the pending packets
            try {
                this.condition.await(); // wait for a packet ot arrive
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Packet<Data> remove = (Packet<Data>) this.pending.remove(0); // get the newly received packet
            this.lock.unlock(); // unlock
            return remove;
        }
        return (Packet<Data>) this.pending.remove(0); // get the newest packet
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean isOpen() {
        return true;
    }
}
