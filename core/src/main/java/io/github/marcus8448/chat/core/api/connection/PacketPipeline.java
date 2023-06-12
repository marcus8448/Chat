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

package io.github.marcus8448.chat.core.api.connection;

import io.github.marcus8448.chat.core.api.crypto.CryptoConstants;
import io.github.marcus8448.chat.core.impl.connection.NetworkPipeline;
import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.PacketType;
import io.github.marcus8448.chat.core.network.packet.Packet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public interface PacketPipeline {
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull PacketPipeline createNetworked(int packetId, @NotNull Socket socket) {
        try {
            return new NetworkPipeline(packetId, socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void setDataTransformer(DataTransformer transformer);

    <Data extends NetworkedData> void send(PacketType<Data> type, Data networkedData) throws IOException;

    <Data extends NetworkedData> Packet<Data> receivePacket() throws IOException;

    <Data extends NetworkedData> Packet<Data> receivePacket(Class<Data> clazz) throws IOException;

    interface DataTransformer {
        static @NotNull DataTransformer encrypted(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
            Cipher encrypt = CryptoConstants.createRsaCipher();
            Cipher decrypt = CryptoConstants.createRsaCipher();
            try {
                encrypt.init(Cipher.ENCRYPT_MODE, publicKey);
                decrypt.init(Cipher.DECRYPT_MODE, privateKey);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            return new DataTransformer() {
                @Override
                public void transform(NetworkedData data, BinaryOutput output) throws IOException {
                    byte[] base = new byte[data.calculateLength()];
                    data.write(BinaryOutput.buffer(base));
                    try {
                        byte[] bytes = encrypt.doFinal(base);
                        output.writeByteArray(bytes);
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public <Data extends NetworkedData> Data transform(PacketType<Data> type, BinaryInput input) throws IOException {
                    byte[] bytes = input.readByteArray();
                    try {
                        byte[] bytes1 = decrypt.doFinal(bytes);
                        return type.create(BinaryInput.wrap(bytes1));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        DataTransformer NO_TRANSFORM = new DataTransformer() {
            @Override
            public void transform(NetworkedData data, BinaryOutput output) throws IOException {
                output.writeShort(data.calculateLength());
                data.write(output);
            }

            @Override
            public <Data extends NetworkedData> Data transform(PacketType<Data> type, BinaryInput input) throws IOException {
                int i = input.readShort();
                return type.create(BinaryInput.wrap(input.readByteArray(i)));
            }
        };

        void transform(NetworkedData data, BinaryOutput output) throws IOException;
        <Data extends NetworkedData> Data transform(PacketType<Data> type, BinaryInput input) throws IOException;
    }
}
