package io.github.marcus8448.chat.core.network.packet;

import io.github.marcus8448.chat.core.api.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.connection.BinaryOutput;
import io.github.marcus8448.chat.core.network.NetworkedData;

import java.io.IOException;

public class EmptyRequest implements NetworkedData {
    public EmptyRequest(BinaryInput input) {}

    @Override
    public void write(BinaryOutput output) throws IOException {}
}
