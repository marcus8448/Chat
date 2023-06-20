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

package io.github.marcus8448.chat.core.api.network.packet.server;

import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;

import java.io.IOException;

public class SystemMessage implements NetworkedData {
    private final long timestamp;
    private final String contents;
    private final byte[] checksum;

    public SystemMessage(long timestamp, String contents, byte[] checksum) {
        this.timestamp = timestamp;
        this.contents = contents;
        this.checksum = checksum;
    }

    public SystemMessage(BinaryInput input) throws IOException {
        this.timestamp = input.readLong();
        this.contents = input.readString();
        this.checksum = input.readByteArray();
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeLong(this.timestamp);
        output.writeString(this.contents);
        output.writeByteArray(this.checksum);
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public String getContents() {
        return contents;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
