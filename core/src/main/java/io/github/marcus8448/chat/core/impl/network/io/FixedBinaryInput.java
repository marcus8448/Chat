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

/**
 * Binary input that wraps an array of fixed size
 * Will throw if it runs out of space
 */
public class FixedBinaryInput extends BaseBinaryInput {
    /**
     * The backing array
     */
    private final byte[] bytes;
    /**
     * The number of bytes written to this input
     */
    private int pos = 0;

    public FixedBinaryInput(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int readByte() {
        return this.bytes[this.pos++];
    }

    @Override
    public void close() {
    }
}
