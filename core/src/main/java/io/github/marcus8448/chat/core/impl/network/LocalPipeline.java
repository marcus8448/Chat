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

import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalPipeline implements PacketPipeline {
    private final List<Packet<?>> pending = new ArrayList<>();
    private LocalPipeline peer = null;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public LocalPipeline() {
    }

    public void setPeer(@NotNull LocalPipeline peer) {
        if (this.peer != null) throw new UnsupportedOperationException();
        this.peer = peer;
    }

    private <Data extends NetworkedData> void receive(Packet<Data> packet) {
        this.lock.lock();
        this.pending.add(packet);
        this.condition.signal();
        this.lock.unlock();
    }

    @Override
    public @NotNull PacketPipeline encryptWith(@NotNull RSAPublicKey sendingKey, @NotNull RSAPrivateKey receivingKey) throws IOException {
        return this; // encryption is pointless.
    }

    @Override
    public <Data extends NetworkedData> void send(PacketType<Data> type, Data networkedData) throws IOException {
        this.peer.receive(new Packet<>(type, networkedData));
    }

    @Override
    public <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException {
        if (this.pending.isEmpty()) {
            this.lock.lock();
            try {
                this.condition.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Packet<Data> remove = (Packet<Data>) this.pending.remove(0);
            this.lock.unlock();
            return remove;
        }
        return (Packet<Data>) this.pending.remove(0);
    }

    @Override
    public <Data extends NetworkedData> Packet<Data> receivePacket(PacketType<Data> type) throws IOException {
        Packet<NetworkedData> packet = this.receivePacket();
        if (packet.type() != type) {
            return this.receivePacket(type);
        }
        return (Packet<Data>) packet;
    }

    @Override
    public void close() throws IOException {}

    @Override
    public boolean isOpen() {
        return true;
    }
}
