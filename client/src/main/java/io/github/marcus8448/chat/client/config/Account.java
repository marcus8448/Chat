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
import io.github.marcus8448.chat.core.api.crypto.CryptoConstants;

import java.lang.reflect.Type;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public record Account(String username, byte[] privateKey, RSAPublicKey publicKey) {
    // PRIVATE KEY IS ENCRYPTED, PUBLIC IS NOT
    public static class Serializer implements JsonSerializer<Account>, JsonDeserializer<Account> {

        @Override
        public Account deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonObject obj = json.getAsJsonObject();
                String username = obj.get("username").getAsString();
                byte[] privateKey = Base64.getDecoder().decode(obj.get("private_key").getAsString());
                byte[] publicKeyE = Base64.getDecoder().decode(obj.get("public_key").getAsString());
                return new Account(username, privateKey, (RSAPublicKey) CryptoConstants.RSA_KEY_FACTORY.generatePublic(new X509EncodedKeySpec(publicKeyE)));
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Account src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("username", src.username());
            obj.addProperty("private_key", Base64.getEncoder().encodeToString(src.privateKey));
            obj.addProperty("public_key", Base64.getEncoder().encodeToString(src.publicKey.getEncoded()));
            return obj;
        }
    }
}
