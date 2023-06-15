package io.github.marcus8448.chat.server.thread;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Deprecated
public class ThreadPerTaskExecutor extends AbstractExecutorService {
    private boolean shutdown = false;

    @Override
    public void shutdown() {
        this.shutdown = true;
    }

    @NotNull
    @Override
    public List<Runnable> shutdownNow() {
        this.shutdown();
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return this.shutdown;
    }

    @Override
    public boolean isTerminated() {
        return this.shutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        this.shutdown();
        return this.shutdown;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        Thread thread = new Thread(command);
        thread.start();
    }
}
