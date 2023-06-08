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

import io.github.marcus8448.chat.core.api.connection.BinaryOutput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class OutputStreamOutput implements BinaryOutput {
    private final OutputStream parent;

    public OutputStreamOutput(OutputStream parent) {
        this.parent = parent;
    }

    @Override
    public void writeByte(int b) throws IOException {
        if (b > 255) throw new UnsupportedEncodingException();
        this.parent.write(b);
    }
    
    @Override
    public void writeShort(int value) throws IOException {
        this.writeByte(value >> 8 & 0xFF);
        this.writeByte(value & 0xFF);
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        this.writeByte(b ? 1 : 0);
    }

    @Override
    public void writeInt(int value) throws IOException { //probably some signed-ness shenanigans here
        this.writeByte(value >> 24 & 0xFF);
        this.writeByte(value >> 16 & 0xFF);
        this.writeByte(value >> 8 & 0xFF);
        this.writeByte(value & 0xFF);
    }

    @Override
    public void writeByteArray(byte @NotNull [] bytes) throws IOException {
        this.writeShort(bytes.length);
        this.writeByteArray(bytes.length, bytes);
    }

    @Override
    public void writeByteArray(int len, byte @NotNull [] bytes) throws IOException {
        assert len == bytes.length;
        this.parent.write(bytes);
    }

    @Override
    public void writeIntArray(int @NotNull [] ints) throws IOException {
        this.writeShort(ints.length);
        this.writeIntArray(ints.length, ints);
    }

    @Override
    public void writeIntArray(int len, int @NotNull [] ints) throws IOException {
        assert ints.length == len;
        for (int i : ints) {
            this.writeInt(i);
        }
    }

    @Override
    public void writeString(@NotNull String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        this.writeByteArray(bytes);
    }

    @Override
    public void writeString(int len, @NotNull String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        assert bytes.length == len;
        this.writeByteArray(len, bytes);
    }


    @Override
    public void close() throws IOException {
        this.parent.close();
    }

//    public void writeLong(long value) throws IOException {
//        this.writeByte((int) (value >> 56 & 0xFF));
//        this.writeByte((int) (value >> 48 & 0xFF));
//        this.writeByte((int) (value >> 40 & 0xFF));
//        this.writeByte((int) (value >> 32 & 0xFF));
//        this.writeByte((int) (value >> 24 & 0xFF));
//        this.writeByte((int) (value >> 16 & 0xFF));
//        this.writeByte((int) (value >> 8 & 0xFF));
//        this.writeByte((int) (value & 0xFF));
//    }
}
