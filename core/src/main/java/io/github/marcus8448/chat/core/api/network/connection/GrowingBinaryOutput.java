package io.github.marcus8448.chat.core.api.network.connection;

import io.github.marcus8448.chat.core.impl.network.connection.GrowingBinaryOutputImpl;

public interface GrowingBinaryOutput extends CountingBinaryOutput {
    static GrowingBinaryOutput create(int baseSize) {
        return new GrowingBinaryOutputImpl(baseSize);
    }

    byte[] getRawOutput();

    byte[] getSizedOutput();
}
