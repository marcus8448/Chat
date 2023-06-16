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

package io.github.marcus8448.chat.server.thread;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

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
