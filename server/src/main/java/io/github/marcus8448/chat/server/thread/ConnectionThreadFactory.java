package io.github.marcus8448.chat.server.thread;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class ConnectionThreadFactory implements ThreadFactory {
    private int number = 1;

    public ConnectionThreadFactory() {}

    @Override
    public Thread newThread(@NotNull Runnable r) {
        return new Thread(r, "Client Connection #" + number);
    }
}
