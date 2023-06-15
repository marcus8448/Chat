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
        for(int i = 0; i < n; i++) {
            buffer.put(i, buffer.get());
        }
        buffer.position(0);
        buffer.limit(n);
    }
}
