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

import io.github.marcus8448.chat.core.util.Utils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Locale;

public class CryptoHelper {
    public static final KeyPairGenerator RSA_KEY_GENERATOR;
    public static final KeyFactory RSA_KEY_FACTORY;
    public static final SecretKeyFactory PBKDF2_SECRET_KEY_FACTORY;
    private static final MessageDigest SHA256_DIGEST = createSha256Digest();

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

    public static Cipher createAesCipher() { //NOT thread safe - so use local
        try {
            return Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Cipher createRsaCipher() {
        try {
            return Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Signature createRsaSignature() {
        try {
            return Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static MessageDigest createSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha256Hash(byte[] key) {
        return Utils.toHexString(SHA256_DIGEST.digest(key)).toUpperCase(Locale.ROOT);
    }
}
