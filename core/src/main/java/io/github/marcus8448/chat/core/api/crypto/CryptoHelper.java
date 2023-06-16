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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;

public class CryptoHelper {
    public static final KeyGenerator AES_KEY_GENERATOR;
    public static final KeyPairGenerator RSA_KEY_GENERATOR;
    private static final KeyFactory RSA_KEY_FACTORY;
    private static final SecretKeyFactory PBKDF2_SECRET_KEY_FACTORY;
    private static final MessageDigest SHA256_DIGEST = createSha256Digest();

    public static String sha256Hash(byte[] key) {
        return Utils.toHexString(SHA256_DIGEST.digest(key)).toUpperCase(Locale.ROOT);
    }

    public static RSAPrivateCrtKey decodeRsaPrivateKey(byte[] bytes) throws InvalidKeySpecException {
        return (RSAPrivateCrtKey) RSA_KEY_FACTORY.generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    public static RSAPublicKey decodeRsaPublicKey(byte[] bytes) throws InvalidKeySpecException {
        return (RSAPublicKey) RSA_KEY_FACTORY.generatePublic(new X509EncodedKeySpec(bytes));
    }

    @Contract("_, _ -> new")
    public static @NotNull SecretKey generateUserPassKey(char[] password, String username) throws InvalidKeySpecException {
        return decodeAesKey(CryptoHelper.PBKDF2_SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(password, username.getBytes(StandardCharsets.UTF_8), 65536, 256)).getEncoded());
    }

    public static Cipher createAesCipher() {
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


    static {
        try {
            AES_KEY_GENERATOR = KeyGenerator.getInstance("AES");
            AES_KEY_GENERATOR.init(256, SecureRandom.getInstanceStrong());

            RSA_KEY_FACTORY = KeyFactory.getInstance("RSA");
            RSA_KEY_GENERATOR = KeyPairGenerator.getInstance("RSA");
            RSA_KEY_GENERATOR.initialize(4096, SecureRandom.getInstanceStrong());

            PBKDF2_SECRET_KEY_FACTORY = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull SecretKey decodeAesKey(byte[] encodedKey) throws InvalidKeySpecException {
        return new SecretKeySpec(encodedKey, "AES");
    }
}
