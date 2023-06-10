package io.github.marcus8448.chat.core.util;

import java.util.HexFormat;
import java.util.Locale;

public class Utils {
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    public static String keyId(byte[] key) {
        return toHexString(key).substring(128, 144).toUpperCase(Locale.ROOT);
    }

    public static String toHexString(byte[] bytes) {
        return HEX_FORMAT.formatHex(bytes);
    }
}
