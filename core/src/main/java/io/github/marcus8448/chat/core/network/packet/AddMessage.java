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

package io.github.marcus8448.chat.core.network.packet;

import io.github.marcus8448.chat.core.api.network.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.network.connection.BinaryOutput;
import io.github.marcus8448.chat.core.network.NetworkedData;

import java.io.IOException;

public class AddMessage implements NetworkedData {
    private final long timestamp;
    private final int authorId;
    private final String contents;
    private final byte[] checksum;

    public AddMessage(BinaryInput input) throws IOException {
        this.timestamp = input.readLong();
        this.authorId = input.readInt();
        this.contents = input.readString();
        this.checksum = input.readByteArray();
    }

    public AddMessage(long timestamp, int authorId, String contents, byte[] checksum) {
        this.timestamp = timestamp;
        this.authorId = authorId;
        this.contents = contents;
        this.checksum = checksum;
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeLong(this.timestamp);
        output.writeInt(this.authorId);
        output.writeString(this.contents);
        output.writeByteArray(this.checksum);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getAuthorId() {
        return authorId;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public String getContents() {
        return contents;
    }
}
