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

package io.github.marcus8448.chat.test;

import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryIOTests {
    private BinaryInput input;
    private BinaryOutput output;

    @BeforeEach
    void initialize() {
        byte[] bytes = new byte[128];
        this.input = BinaryInput.buffer(bytes);
        this.output = BinaryOutput.buffer(bytes);
    }

    @Test
    void testWriteByte() throws IOException {
        this.output
                .writeByte(0)
                .writeByte(Byte.MAX_VALUE)
                .writeByte(Byte.MIN_VALUE)
                .writeByte(4)
                .writeByte(63)
                .writeByte(24)
                .writeByte(-12)
                .writeByte(-79);

        assertEquals(0, this.input.readByte());
        assertEquals(Byte.MAX_VALUE, this.input.readByte());
        assertEquals(Byte.MIN_VALUE, this.input.readByte());
        assertEquals(4, this.input.readByte());
        assertEquals(63, this.input.readByte());
        assertEquals(24, this.input.readByte());
        assertEquals(-12, this.input.readByte());
        assertEquals(-79, this.input.readByte());
    }

    @Test
    void testWriteShort() throws IOException {
        this.output
                .writeShort(0)
                .writeShort(Short.MAX_VALUE)
                .writeShort(Short.MIN_VALUE)
                .writeShort(1289)
                .writeShort(-4897)
                .writeShort(384)
                .writeShort(-213);

        assertEquals(0, this.input.readShort());
        assertEquals(Short.MAX_VALUE, this.input.readShort());
        assertEquals(Short.MIN_VALUE, this.input.readShort());
        assertEquals(1289, this.input.readShort());
        assertEquals(-4897, this.input.readShort());
        assertEquals(384, this.input.readShort());
        assertEquals(-213, this.input.readShort());
    }

    @Test
    void testWriteInt() throws IOException {
        this.output
                .writeInt(0)
                .writeInt(Integer.MAX_VALUE)
                .writeInt(Integer.MIN_VALUE)
                .writeInt(Integer.MAX_VALUE - 1)
                .writeInt(Integer.MIN_VALUE + 1)
                .writeInt(2789317)
                .writeInt(-772248)
                .writeInt(684)
                .writeInt(70)
                .writeInt(-49);

        assertEquals(0, this.input.readInt());
        assertEquals(Integer.MAX_VALUE, this.input.readInt());
        assertEquals(Integer.MIN_VALUE, this.input.readInt());
        assertEquals(Integer.MAX_VALUE - 1, this.input.readInt());
        assertEquals(Integer.MIN_VALUE + 1, this.input.readInt());
        assertEquals(2789317, this.input.readInt());
        assertEquals(-772248, this.input.readInt());
        assertEquals(684, this.input.readInt());
        assertEquals(70, this.input.readInt());
        assertEquals(-49, this.input.readInt());
    }
}
