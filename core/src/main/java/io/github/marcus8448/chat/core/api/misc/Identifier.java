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

package io.github.marcus8448.chat.core.api.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Effectively, a string with extra constraints.
 * <p>
 * Only allows string with these characters: [0-9], [a-Z], _ and of length 4-16
 */
public class Identifier {
    /**
     * The wrapped string
     */
    private final @NotNull String value;

    private Identifier(@NotNull String value) {
        this.value = value;
    }

    /**
     * Creates a new identifier
     *
     * @param value the name of the identifier
     * @return a new identifier
     * @throws IllegalArgumentException if the string contains illegal characters
     */
    public static @NotNull Identifier create(String value) {
        String failure = Identifier.failureReason(value);
        if (failure != null) throw new IllegalArgumentException(failure);
        return new Identifier(value);
    }

    /**
     * Optionally creates a new identifier
     *
     * @param value the name of the identifier
     * @return a new identifier, or an error message saying why it was invalid
     */
    public static @NotNull Result<Identifier, String> parse(String value) {
        String failure = Identifier.failureReason(value);
        if (failure == null) return Result.ok(new Identifier(value));
        return Result.error(failure);
    }

    /**
     * Verifies the validity of a string to become an identifier
     *
     * @param s the string to test
     * @return whether the string is a valid identifier
     */
    public static boolean verify(String s) {
        if (s.length() < 4 || s.length() > 16) return false;
        for (char c : s.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '_') {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies the validity of a string to become an identifier
     *
     * @param s the string to test
     * @return whether the string is a valid identifier
     */
    public static @Nullable String failureReason(String s) {
        if (s.length() < 4) {
            return "Too short";
        } else if (s.length() > 16) {
            return "Too long";
        }

        for (char c : s.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '_') {
                return "Non [a-Z] [0-9] _ character!";
            }
        }
        return null;
    }

    public @NotNull String getValue() {
        return this.value;
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
        return this.getValue();
    }
}
