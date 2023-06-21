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

package io.github.marcus8448.chat.core.api.network.io;

import io.github.marcus8448.chat.core.api.misc.Identifier;
import io.github.marcus8448.chat.core.impl.network.io.FixedBinaryOutput;
import io.github.marcus8448.chat.core.impl.network.io.OutputStreamOutput;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents an outgoing stream of bytes (similar to an {@link OutputStream}), but with more helper methods
 */
public interface BinaryOutput extends Closeable {
    @Contract(value = "_ -> new", pure = true)
    static @NotNull BinaryOutput stream(OutputStream outputStream) {
        return new OutputStreamOutput(outputStream);
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull BinaryOutput buffer(byte[] wrapped) {
        return new FixedBinaryOutput(wrapped);
    }

    BinaryOutput writeByte(int b) throws IOException;

    BinaryOutput writeInt(int i) throws IOException;

    BinaryOutput writeShort(int s) throws IOException;

    BinaryOutput writeLong(long l) throws IOException;

    BinaryOutput writeBoolean(boolean b) throws IOException;

    BinaryOutput writeByteArray(byte @NotNull [] bytes) throws IOException;

    BinaryOutput writeByteArray(int len, byte @NotNull [] bytes) throws IOException;

    BinaryOutput writeIntArray(int @NotNull [] ints) throws IOException;

    BinaryOutput writeIntArray(int len, int @NotNull [] ints) throws IOException;

    BinaryOutput writeString(@NotNull String s) throws IOException;

    BinaryOutput writeString(int len, @NotNull String s) throws IOException;

    BinaryOutput writeIdentifier(@NotNull Identifier id) throws IOException;

    @Override
    void close() throws IOException;
}
