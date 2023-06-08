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

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class CryptoConstants {
    public static final KeyPairGenerator RSA_KEY_GENERATOR;
    public static final KeyFactory RSA_KEY_FACTORY;
    public static final SecretKeyFactory PBKDF2_SECRET_KEY_FACTORY;
    private static final ThreadLocal<Cipher> AES_CIPHER = ThreadLocal.withInitial(() -> { //NOT thread safe - so use local
        try {
            return  Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    });
    private static final ThreadLocal<Cipher> RSA_CIPHER = ThreadLocal.withInitial(() -> {
        try {
            return  Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    });

    public static Cipher getRsaCipher() {
        return RSA_CIPHER.get();
    }

    static {
        try {
            RSA_KEY_FACTORY = KeyFactory.getInstance("RSA");
            RSA_KEY_GENERATOR = KeyPairGenerator.getInstance("RSA");
            RSA_KEY_GENERATOR.initialize(4096);
            PBKDF2_SECRET_KEY_FACTORY = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Cipher getAesCipher() {
        return AES_CIPHER.get();
    }
}
