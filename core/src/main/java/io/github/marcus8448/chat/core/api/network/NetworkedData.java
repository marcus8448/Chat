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

package io.github.marcus8448.chat.core.api.network;

import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;

import java.io.IOException;

/**
 * Data that can be sent over the network
 *
 * @see io.github.marcus8448.chat.core.api.network.packet.PacketType
 */
public interface NetworkedData {
    /**
     * @return the serialized length of this data, if available (otherwise -1)
     */
    default int getLength() {
        return -1;
    }

    /**
     * Writes the data contained in this type to the stream
     *
     * @param output the place to write the data
     */
    void write(BinaryOutput output) throws IOException;
}
