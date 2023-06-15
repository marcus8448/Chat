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

import io.github.marcus8448.chat.core.api.network.connection.GrowingBinaryOutput;

import java.io.IOException;

public class GrowingBinaryOutputImpl extends BaseBinaryOutput implements GrowingBinaryOutput {
    private byte[] data;
    private int pos = 0;

    public GrowingBinaryOutputImpl(int baseSize) {
        this.data = new byte[baseSize];
    }

    @Override
    public void writeByte(int b) {
        if (this.pos == this.data.length) {
            byte[] allocated = new byte[this.data.length * 2]; //todo: 1.5x better?
            System.arraycopy(this.data, 0, allocated, 0, this.data.length);
            this.data = allocated;
        }
        this.data[this.pos++] = (byte) b;
    }

    @Override
    public void close() throws IOException {}

    @Override
    public int getCount() {
        return this.pos;
    }

    @Override
    public byte[] getRawOutput() {
        return this.data;
    }

    @Override
    public byte[] getSizedOutput() {
        byte[] out = new byte[this.pos];
        System.arraycopy(this.data, 0, out, 0, this.pos);
        return out;
    }
}
