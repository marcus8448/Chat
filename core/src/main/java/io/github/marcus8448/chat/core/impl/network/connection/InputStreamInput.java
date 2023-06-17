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

import java.io.IOException;
import java.io.InputStream;

/**
 * Binary input that wraps an input stream
 */
public class InputStreamInput extends BaseBinaryInput {
    private final InputStream parent;

    public InputStreamInput(InputStream parent) {
        this.parent = parent;
    }

    @Override
    public int readByte() throws IOException {
        return this.parent.read();
    }

    @Override
    public void close() throws IOException {
        this.parent.close();
    }
}
