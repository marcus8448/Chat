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

package io.github.marcus8448.chat.server.util;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

public class Utils {
    private static final AtomicLong previousTime = new AtomicLong(0);

    // clock that always goes up
    public static long currentTimeNonDecreasing() {
        long l = System.currentTimeMillis();
        long l1 = previousTime.longValue();
        if (l1 >= l) return previousTime.incrementAndGet();
        if (previousTime.compareAndSet(l1, l)) {
            return l;
        } else {
            return currentTimeNonDecreasing();
        }
    }

    public static void shiftLeft(ByteBuffer buffer) {
        int n = buffer.limit() - buffer.position();
        for (int i = 0; i < n; i++) {
            buffer.put(i, buffer.get());
        }
        buffer.position(0);
        buffer.limit(n);
    }
}
