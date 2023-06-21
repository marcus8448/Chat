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

package io.github.marcus8448.chat.client.util;

import io.github.marcus8448.chat.core.api.misc.Result;

public class ParseUtil {
    /**
     * Validates the given password to be secure.
     * 1+ digit, lowercase letter, uppercase letter, and symbol.
     * Length: 8+ characters.
     *
     * @param password the password is secure
     * @return Ok, if the password is secure, otherwise the reason it is insecure
     */
    public static Result<Void, String> validatePassword(String password) {
        if (password.length() < 8) return Result.error("Too short");
        boolean digit = false, lower = false, upper = false, symbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                digit = true;
            } else if (Character.isLetter(c)) {
                if (Character.isUpperCase(c)) {
                    upper = true;
                } else {
                    lower = true;
                }
            } else {
                symbol = true;
            }
        }
        if (!digit) {
            return Result.error("Missing digit (0-9)");
        }
        if (!upper) {
            return Result.error("Missing uppercase (A-Z)");
        }
        if (!lower) {
            return Result.error("Missing lowercase (a-z)");
        }
        if (!symbol) {
            return Result.error("Missing symbol");
        }
        return Result.ok(null);
    }
}
