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

package io.github.marcus8448.chat.core.impl.connection;

import io.github.marcus8448.chat.core.api.connection.BinaryInput;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class InputStreamInput implements BinaryInput {
    private final InputStream parent;

    public InputStreamInput(InputStream parent) {
        this.parent = parent;
    }

    @Override
    public int readByte() throws IOException {
        return this.parent.read();
    }

    @Override
    public int readInt() throws IOException {
        return this.readByte() << 24 | this.readByte() << 16 | this.readByte() << 8 | this.readByte();
    }

    @Override
    public int readShort() throws IOException {
        return this.readByte() << 8 | this.readByte();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return this.readByte() == 1;
    }

    @Override
    public byte[] readByteArray(int len) throws IOException {
        return this.parent.readNBytes(len);
    }

    @Override
    public byte[] readByteArray() throws IOException {
        return this.parent.readNBytes(this.readShort());
    }

    @Override
    public int[] readIntArray() throws IOException {
        return this.readIntArray(this.readShort());
    }

    @Override
    public int[] readIntArray(int len) throws IOException {
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = this.readInt();
        }
        return arr;
    }

    @Override
    public String readString() throws IOException {
        return new String(this.readByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public String readString(int len) throws IOException {
        return new String(this.readByteArray(len), StandardCharsets.UTF_8);
    }

    @Override
    public void seekToIdentifier(int id) throws IOException {
        int p0 = id >> 24 & 0xFF;
        int p1 = id >> 16 & 0xFF;
        int p2 = id >> 8 & 0xFF;
        int p3 = id & 0xFF;
        System.out.println("Expecting: " + p0 + "-" + p1 + "-" + p2 + "-" + p3);
        int read = this.readByte();
        while (true) {
            if (read == -1) throw new EOFException();
            if (read == p0) {
                read = this.readByte();
                if (read == p1) {
                    read = this.readByte();
                    if (read == p2) {
                        read = this.readByte();
                        if (read == p3) {
                            System.out.println("Received packet header.");
                            return;
                        }
                    }
                }
            }
            read = this.readByte();
        }
    }

    @Override
    public void close() throws IOException {
        this.parent.close();
    }
}