package io.github.marcus8448.chat.core.util;

import java.util.Locale;

public class Utils {
    public static String keyId(byte[] key) {
        int len = key.length - 10;
        return (Integer.toHexString(key[len - 8] << 24 | (key[len - 7] & 0xFF) << 16 | (key[len - 6] & 0xFF) << 8 | key[len - 5] & 0xFF) + Integer.toHexString(key[len - 4] << 24 | (key[len - 3] & 0xFF) << 16 | (key[len - 2] & 0xFF) << 8 | key[len - 1] & 0xFF)).toUpperCase(Locale.ROOT);
    }
}
