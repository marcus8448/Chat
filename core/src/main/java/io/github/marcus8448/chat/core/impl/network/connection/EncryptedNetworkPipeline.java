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

package io.github.marcus8448.chat.core.impl.network.connection;

import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.network.connection.BinaryOutput;
import io.github.marcus8448.chat.core.api.network.connection.GrowingBinaryOutput;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.packet.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;

public class EncryptedNetworkPipeline implements PacketPipeline {
    private static final Logger LOGGER = LogManager.getLogger();

    private final int packetHeader;
    private final Socket socket;
    private final BinaryInput input;
    private final BinaryOutput output;
    private final Cipher decryption = CryptoHelper.createAesCipher();
    private final Cipher encryption = CryptoHelper.createAesCipher();

    public EncryptedNetworkPipeline(int packetHeader, @NotNull Socket socket, @NotNull BinaryInput input, @NotNull BinaryOutput output, @NotNull SecretKey secretKey) throws IOException {
        this.packetHeader = packetHeader;
        this.socket = socket;
        this.input = input;
        this.output = output;
        try {
            this.encryption.init(Cipher.ENCRYPT_MODE, secretKey);
            this.decryption.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull PacketPipeline encryptWith(@NotNull SecretKey secretKey) throws IOException {
        return new EncryptedNetworkPipeline(this.packetHeader, this.socket, this.input, this.output, secretKey);
    }

    @Override
    public synchronized <Data extends NetworkedData> void send(PacketType<Data> type, Data networkedData) throws IOException {
        LOGGER.debug("Sending packet {}", type.getDataClass().getName());
        this.output.writeInt(this.packetHeader);
        int len = networkedData.getLength();
        byte[] data;
        if (len != -1) {
            data = new byte[2 + len]; // write packet ID
            BinaryOutput fixed = BinaryOutput.buffer(data);
            fixed.writeShort(type.getId());
            networkedData.write(fixed);
        } else {
            GrowingBinaryOutput clear = GrowingBinaryOutput.create(128);
            clear.writeShort(type.getId());
            networkedData.write(clear);
            data = clear.getRawOutput();
            len = clear.getCount();
        }
        byte[] bytes;
        try {
            bytes = this.encryption.doFinal(data, 0, len);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        this.output.writeByteArray(bytes);
    }

    @Override
    public <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException {
        this.input.seekToIdentifier(this.packetHeader);
        byte[] bytes = this.input.readByteArray();
        byte[] clear;
        try {
            clear = this.decryption.doFinal(bytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        BinaryInput input = BinaryInput.wrap(clear);
        PacketType<Data> type = (PacketType<Data>) PacketType.getType(input.readShort());
        LOGGER.debug("Received packet {}", type.getDataClass().getName());
        return new Packet<>(type, type.create(input));
    }

    @Override
    public <Data extends NetworkedData> Packet<Data> receivePacket(PacketType<Data> type) throws IOException {
        Packet<NetworkedData> packet = this.receivePacket();
        if (packet.type() != type) {
            LOGGER.debug("Discarding packet {}", type.getDataClass().getName());
            return this.receivePacket(type);
        }
        LOGGER.debug("Received packet {}", type.getDataClass().getName());
        return (Packet<Data>) packet;
    }

    @Override
    public void close() throws IOException {
        this.input.close();
        this.output.close();
    }

    @Override
    public boolean isOpen() {
        return !this.socket.isClosed();
    }
}
