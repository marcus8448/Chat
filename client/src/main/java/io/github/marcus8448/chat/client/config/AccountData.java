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

package io.github.marcus8448.chat.client.config;

import com.google.gson.*;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.crypto.Encrypted;
import io.github.marcus8448.chat.core.api.crypto.PrivateData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Private data associated with an account
 *
 * @param privateKey    the full RSA private key of the account
 * @param knownAccounts map of user public key -> name, for accounts that have been manually trusted
 * @param knownServers  map of "hostname:port" -> public key, to identify trusted servers and avoid MitM attacks
 */
public record AccountData(RSAPrivateKey privateKey, Map<RSAPublicKey, String> knownAccounts, Map<String, RSAPublicKey> knownServers) implements PrivateData<AccountData.EncryptedAccountData> {
    @Override
    public EncryptedAccountData encrypt(Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
        Map<byte[], byte[]> encodedAccounts = new HashMap<>(knownAccounts.size());
        for (Map.Entry<RSAPublicKey, String> entry : this.knownAccounts.entrySet()) {
            encodedAccounts.put(cipher.doFinal(entry.getKey().getEncoded()), cipher.doFinal(entry.getValue().getBytes(StandardCharsets.UTF_8)));
        }
        Map<byte[], byte[]> encodedServers = new HashMap<>(this.knownServers.size());
        for (Map.Entry<String, RSAPublicKey> entry : this.knownServers.entrySet()) {
            encodedServers.put(
                    cipher.doFinal(entry.getKey().getBytes(StandardCharsets.UTF_8)),
                    cipher.doFinal(entry.getValue().getEncoded())
            );
        }
        return new EncryptedAccountData(cipher.doFinal(privateKey.getEncoded()), encodedAccounts, encodedServers);
    }

    /**
     * Encrypted form of account data. Must be decrypted to be useful
     *
     * @see AccountData
     */
    public record EncryptedAccountData(byte[] privateKey, Map<byte[], byte[]> knownAccounts,
                                       Map<byte[], byte[]> knownServers) implements Encrypted<AccountData> {
        @Contract("_ -> new")
        @Override
        public @NotNull AccountData decrypt(@NotNull Cipher cipher) throws IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
            RSAPrivateKey rsaPrivateKey = CryptoHelper.decodeRsaPrivateKey(cipher.doFinal(privateKey));
            Map<RSAPublicKey, String> decodedAccounts = new HashMap<>();
            for (Map.Entry<byte[], byte[]> entry : this.knownAccounts.entrySet()) {
                decodedAccounts.put(CryptoHelper.decodeRsaPublicKey(cipher.doFinal(entry.getKey())), new String(cipher.doFinal(entry.getValue()), StandardCharsets.UTF_8));
            }
            Map<String, RSAPublicKey> decodedServers = new HashMap<>();
            for (Map.Entry<byte[], byte[]> entry : this.knownServers.entrySet()) {
                decodedServers.put(new String(cipher.doFinal(entry.getKey()), StandardCharsets.UTF_8), CryptoHelper.decodeRsaPublicKey(cipher.doFinal(entry.getValue())));
            }
            return new AccountData(rsaPrivateKey, decodedAccounts, decodedServers);
        }

        /**
         * Serializes the data for a config file
         * All encrypted values are encoded in base64 before being written (since it's a JSON/text config)
         */
        public static class Serializer implements JsonSerializer<EncryptedAccountData>, JsonDeserializer<EncryptedAccountData> {
            public static final Serializer INSTANCE = new Serializer();

            private Serializer() {
            }

            @Override
            public EncryptedAccountData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                Base64.Decoder decoder = Base64.getDecoder();
                JsonObject obj = json.getAsJsonObject();
                Map<String, JsonElement> accountsJ = obj.get("known_accounts").getAsJsonObject().asMap();
                Map<String, JsonElement> serversJ = obj.get("known_servers").getAsJsonObject().asMap();
                Map<byte[], byte[]> knownAccounts = new HashMap<>();
                accountsJ.forEach((k, v) -> knownAccounts.put(decoder.decode(k), decoder.decode(v.getAsString())));
                Map<byte[], byte[]> knownServers = new HashMap<>();
                serversJ.forEach((k, v) -> knownServers.put(decoder.decode(k), decoder.decode(v.getAsString())));
                return new EncryptedAccountData(decoder.decode(obj.get("private_key").getAsString()), knownAccounts, knownServers);
            }

            @Override
            public JsonElement serialize(EncryptedAccountData src, Type typeOfSrc, JsonSerializationContext context) {
                Base64.Encoder encoder = Base64.getEncoder();
                JsonObject object = new JsonObject();
                JsonObject accounts = new JsonObject();
                JsonObject servers = new JsonObject();
                src.knownAccounts.forEach((k, v) -> accounts.addProperty(encoder.encodeToString(k), encoder.encodeToString(v)));
                src.knownServers.forEach((k, v) -> servers.addProperty(encoder.encodeToString(k), encoder.encodeToString(v)));
                object.addProperty("private_key", encoder.encodeToString(src.privateKey));
                object.add("known_accounts", accounts);
                object.add("known_servers", servers);
                return object;
            }
        }
    }
}
