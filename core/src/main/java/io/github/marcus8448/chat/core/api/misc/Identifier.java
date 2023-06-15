package io.github.marcus8448.chat.core.api.misc;

import org.jetbrains.annotations.NotNull;

public class Identifier {
    private final @NotNull String value;

    private Identifier(@NotNull String value) {
        this.value = value;
    }

    public static @NotNull Identifier create(String value) {
        if (!Identifier.verify(value)) throw new IllegalArgumentException("Invalid id!");
        return new Identifier(value);
    }

    public static boolean verify(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '_') {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return this.value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value;
    }
}
