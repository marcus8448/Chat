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

import io.github.marcus8448.chat.core.impl.connection.CountingBinaryOutputImpl;
import io.github.marcus8448.chat.core.impl.connection.FixedBinaryOutput;
import io.github.marcus8448.chat.core.impl.connection.OutputStreamOutput;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public interface BinaryOutput extends Closeable {
    @Contract(value = "_ -> new", pure = true)
    static @NotNull BinaryOutput stream(OutputStream outputStream) {
        return new OutputStreamOutput(outputStream);
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull BinaryOutput buffer(byte[] wrapped) {
        return new FixedBinaryOutput(wrapped);
    }

    static CountingBinaryOutput counting() {
        return new CountingBinaryOutputImpl();
    }

    void writeByte(int b) throws IOException;
    void writeInt(int i) throws IOException;
    void writeShort(int s) throws IOException;

    void writeBoolean(boolean b) throws IOException;

    void writeByteArray(byte @NotNull [] bytes) throws IOException;
    void writeByteArray(int len, byte @NotNull [] bytes) throws IOException;

    void writeIntArray(int @NotNull [] ints) throws IOException;
    void writeIntArray(int len, int @NotNull [] ints) throws IOException;

    void writeString(@NotNull String s) throws IOException;
    void writeString(int len, @NotNull String s) throws IOException;
}
