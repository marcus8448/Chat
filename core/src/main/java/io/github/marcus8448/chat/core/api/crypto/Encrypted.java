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

package io.github.marcus8448.chat.core.api.crypto;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.security.spec.InvalidKeySpecException;

/**
 * Represents encrypted data (that can be decrypted)
 * @param <D> A type that represents the decrypted data
 */
public interface Encrypted<D> {
    /**
     * Decrypts the data represented by this object
     * @param cipher The cipher to use to decrypt
     *               (Must be properly initialized already)
     * @return The decrypted data
     */
    @Contract("_ -> new")
    @NotNull D decrypt(@NotNull Cipher cipher) throws IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException;
}
