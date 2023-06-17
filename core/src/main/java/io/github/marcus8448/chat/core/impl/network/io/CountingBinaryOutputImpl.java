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

import io.github.marcus8448.chat.core.api.network.io.CountingBinaryOutput;

import java.io.IOException;

/**
 * Binary output that just counts the number of bytes written
 * All data is discarded
 */
public class CountingBinaryOutputImpl extends BaseBinaryOutput implements CountingBinaryOutput {
    private int count = 0;

    @Override
    public void writeByte(int b) {
        this.count++;
    }

    @Override
    public void close() throws IOException {}

    @Override
    public int getCount() {
        return this.count;
    }
}
