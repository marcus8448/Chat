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

import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.PacketPipeline;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;
import io.github.marcus8448.chat.core.api.network.io.GrowingBinaryOutput;
import io.github.marcus8448.chat.core.api.network.packet.Packet;
import io.github.marcus8448.chat.core.api.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;

/**
 * A network pipeline that has been encrypted by an AES key
 *
 * @see io.github.marcus8448.chat.core.impl.network.NetworkPacketPipeline
 */
public class EncryptedNetworkPipeline implements PacketPipeline {

    /**
     * The packet header in use
     */
    private final int packetHeader;
    /**
     * The network (socket) connection
     */
    private final Socket socket;
    /**
     * The binary input (wrapped socket input stream)
     */
    private final BinaryInput input;
    /**
     * The binary output (wrapped socket input stream)
     */
    private final BinaryOutput output;

    /**
     * The cipher used for decryption. Initialized with the AES key provided in the constructor
     */
    private final Cipher decryption = CryptoHelper.createAesCipher();
    /**
     * The cipher used for encryption. Initialized with the AES key provided in the constructor
     */
    private final Cipher encryption = CryptoHelper.createAesCipher();

    public EncryptedNetworkPipeline(int packetHeader, @NotNull Socket socket, @NotNull BinaryInput input, @NotNull BinaryOutput output, @NotNull SecretKey secretKey) throws IOException {
        this.packetHeader = packetHeader;
        this.socket = socket;
        this.input = input;
        this.output = output;

        // Initialize ciphers for encryption/decryption
        try {
            this.encryption.init(Cipher.ENCRYPT_MODE, secretKey);
            this.decryption.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull PacketPipeline encryptWith(@NotNull SecretKey secretKey) throws IOException {
        // encryption is NOT recursive
        return new EncryptedNetworkPipeline(this.packetHeader, this.socket, this.input, this.output, secretKey);
    }

    @Override
    public synchronized <Data extends NetworkedData> void send(PacketType<Data> type, Data networkedData) throws IOException {
        this.output.writeInt(this.packetHeader); // write the packet header
        int len = networkedData.getLength(); // get the data size
        byte[] data;
        // check if the size is known/valid
        if (len != -1) {
            len += Short.BYTES; // packet ID (short) size
            data = new byte[len]; // allocate space for the packet (data)
            BinaryOutput fixed = BinaryOutput.buffer(data); // create a buffer for the data
            fixed.writeShort(type.getId()); // write the packet ID
            networkedData.write(fixed); // write the packet data
        } else {
            GrowingBinaryOutput clear = GrowingBinaryOutput.create(128); // create an unbounded buffer
            clear.writeShort(type.getId()); // write the packet ID
            networkedData.write(clear); // write the packet data
            data = clear.getRawOutput(); // get the written data
            len = clear.getCount(); // update the actual length of the data
        }
        byte[] bytes;
        try {
            // encrypt the packet
            bytes = this.encryption.doFinal(data, 0, len);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        // send the data
        this.output.writeInt(bytes.length);
        this.output.writeByteArray(bytes.length, bytes);
    }

    @Override
    public <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException {
        // Wait for a packet header
        this.input.seekToHeader(this.packetHeader);
        // read the (encrypted) data
        byte[] bytes = this.input.readByteArray(this.input.readInt());
        byte[] clear;
        try {
            // decrypt the data
            clear = this.decryption.doFinal(bytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        // wrap the data in a binary input for easy reading
        BinaryInput input = BinaryInput.buffer(clear);
        // get the type of packet based on the short id
        PacketType<Data> type = (PacketType<Data>) PacketType.getType(input.readShort());
        // create the packet
        return new Packet<>(type, type.create(input));
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
