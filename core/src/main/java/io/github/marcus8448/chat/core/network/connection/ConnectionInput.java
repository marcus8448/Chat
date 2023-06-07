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

package io.github.marcus8448.chat.core.network.connection;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;

public class ConnectionInput extends InputStream {
    private final InputStream parent;

    public ConnectionInput(InputStream parent) {
        this.parent = parent;
    }

    @Override
    public int read() throws IOException {
        int read = this.parent.read();
        if (read == -1) throw new ClosedChannelException();
        return read;
    }

    public void seekToIdentifier(int id) throws IOException {
        int p0 = id >> 24 & 0xFF;
        int p1 = id >> 16 & 0xFF;
        int p2 = id >> 8 & 0xFF;
        int p3 = id & 0xFF;
        System.out.println("Expecting: " + p0 + "-" + p1 + "-" + p2 + "-" + p3);
        int read = this.read();
        while (true) {
            if (read == -1) throw new EOFException();
            if (read == p0) {
                read = this.read();
                if (read == p1) {
                    read = this.read();
                    if (read == p2) {
                        read = this.read();
                        if (read == p3) {
                            System.out.println("Received packet header.");
                            return;
                        }
                    }
                }
            }
            read = this.read();
        }
    }

    public int readShort() throws IOException {
        return this.read() << 8 | this.read();
    }

    public int readInt() throws IOException {
        return this.read() << 24 | this.read() << 16 | this.read() << 8 | this.read();
    }

    public String readString() throws IOException {
        int len = this.readShort();
        return this.readString(len);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.parent.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        this.parent.mark(readlimit);
    }

    @Override
    public int available() throws IOException {
        return this.parent.available();
    }

    @Override
    public boolean markSupported() {
        return this.parent.markSupported();
    }

    public String readString(int len) throws IOException {
        byte[] bytes = this.readNBytes(len);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
