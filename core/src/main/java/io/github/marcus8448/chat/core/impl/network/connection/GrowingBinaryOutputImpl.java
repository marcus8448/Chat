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

/**
 * A binary output that grows an array to meet demand
 * By default, the array doubles in size when it reaches the limit
 */
public class GrowingBinaryOutputImpl extends BaseBinaryOutput implements GrowingBinaryOutput {
    /**
     * The backing array
     * Note that the array will almost always be larger than the actual number of written values
     */
    private byte[] data;
    /**
     * The number of written bytes
     */
    private int pos = 0;

    public GrowingBinaryOutputImpl(int baseSize) {
        this.data = new byte[baseSize];
    }

    @Override
    public void writeByte(int b) {
        // check if we have hit the size limit
        if (this.pos == this.data.length) {
            // allocate another array that is double the size
            byte[] allocated = new byte[this.data.length * 2]; //todo: 1.5x better?
            // quickly copy the data to the new array
            System.arraycopy(this.data, 0, allocated, 0, this.data.length);
            // set the backing array to the new array
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
        // create an array that is the size of the written bytes (<= backing array)
        byte[] out = new byte[this.pos];
        // copy the data into this array
        System.arraycopy(this.data, 0, out, 0, this.pos);
        return out; // return it
    }
}
