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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * Represents data that can (and should) be encrypted before serialization
 *
 * @param <E> The type that represents the encrypted data
 */
public interface PrivateData<E> {
    /**
     * Encrypts the data represented by this object
     *
     * @param cipher the cipher to use when encrypting the data
     *               (Must be properly initialized)
     * @return the encrypted data
     */
    E encrypt(Cipher cipher) throws IllegalBlockSizeException, BadPaddingException;
}
