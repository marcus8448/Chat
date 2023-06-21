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
