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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ConnectionOutput extends OutputStream {
    private final OutputStream parent;

    public ConnectionOutput(OutputStream parent) {
        this.parent = parent;
    }

    @Override
    public void write(int b) throws IOException {
        if (b > 255) throw new UnsupportedEncodingException();
        this.parent.write(b);
    }

    public void writeShort(int value) throws IOException {
        this.write(value >> 8 & 0xFF);
        this.write(value & 0xFF);
    }

    public void writeInt(int value) throws IOException { //probably some signed-ness shenanigans here
        this.write(value >> 24 & 0xFF);
        this.write(value >> 16 & 0xFF);
        this.write(value >> 8 & 0xFF);
        this.write(value & 0xFF);
    }

    public void writeString(String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        this.writeShort(bytes.length);
        this.write(bytes);
    }

    public void writeString(int len, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        assert len == bytes.length;
        this.write(bytes);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.parent.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.parent.close();
    }

//    public void writeLong(long value) throws IOException {
//        this.write((int) (value >> 56 & 0xFF));
//        this.write((int) (value >> 48 & 0xFF));
//        this.write((int) (value >> 40 & 0xFF));
//        this.write((int) (value >> 32 & 0xFF));
//        this.write((int) (value >> 24 & 0xFF));
//        this.write((int) (value >> 16 & 0xFF));
//        this.write((int) (value >> 8 & 0xFF));
//        this.write((int) (value & 0xFF));
//    }
}
