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

import io.github.marcus8448.chat.core.impl.network.io.GrowingBinaryOutputImpl;

/**
 * Represents a binary output backed by arrays to match the size of the input
 */
public interface GrowingBinaryOutput extends CountingBinaryOutput {
    static GrowingBinaryOutput create(int baseSize) {
        return new GrowingBinaryOutputImpl(baseSize);
    }

    /**
     * @return the raw byte array backing this output
     */
    byte[] getRawOutput();

    /**
     * @return a copy of the backing byte array that is sized to the current number of bytes inputted
     */
    byte[] getSizedOutput();
}
