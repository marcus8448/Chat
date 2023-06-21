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

package io.github.marcus8448.chat.core.impl.network.io;

import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;

import java.io.IOException;

/**
 * Binary output of fixed size
 * Will throw if it runs out of space
 */
public class FixedBinaryOutput extends BaseBinaryOutput {
    /**
     * The wrapped array
     */
    private final byte[] wrap;
    /**
     * THe number of bytes written to this output
     */
    private int pos = 0;

    public FixedBinaryOutput(byte[] wrap) {
        this.wrap = wrap;
    }

    @Override
    public BinaryOutput writeByte(int b) {
        this.wrap[pos++] = (byte) b;
        return this;
    }

    @Override
    public void close() throws IOException {
    }
}
