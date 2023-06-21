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

package io.github.marcus8448.chat.core.api.network.packet.common;

import io.github.marcus8448.chat.core.api.misc.Identifier;
import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;

import java.io.IOException;

public class ChannelList implements NetworkedData {
    private final Identifier[] channels;

    public ChannelList(Identifier... channels) {
        this.channels = channels;
        if (this.channels.length > 50) throw new UnsupportedOperationException();
    }

    public ChannelList(BinaryInput input) throws IOException {
        int len = input.readByte();
        if (len > 50) throw new UnsupportedOperationException();
        this.channels = new Identifier[len];
        for (int i = 0; i < len; i++) {
            this.channels[i] = input.readIdentifier();
        }
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeByte(this.channels.length);
        for (Identifier channel : this.channels) {
            output.writeIdentifier(channel);
        }
    }

    public Identifier[] getChannels() {
        return channels;
    }
}
