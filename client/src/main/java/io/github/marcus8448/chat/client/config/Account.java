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
import io.github.marcus8448.chat.core.api.misc.Identifier;

import java.lang.reflect.Type;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * A user account (to be serialized into the client config file)
 * Most data is encrypted, so it's not very useful outside of serialization
 *
 * @param username  the username of this account
 * @param publicKey the (RSA) public key of this account
 * @param data      the encrypted data associated with this account
 * @see AccountData
 */
public record Account(Identifier username, RSAPublicKey publicKey, AccountData.EncryptedAccountData data) {
    public static class Serializer implements JsonSerializer<Account>, JsonDeserializer<Account> {
        @Override
        public Account deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonObject obj = json.getAsJsonObject();
                Identifier username = Identifier.parse(obj.get("username").getAsString()).unwrap();
                RSAPublicKey publicKey = CryptoHelper.decodeRsaPublicKey(Base64.getDecoder().decode(obj.get("public_key").getAsString()));
                AccountData.EncryptedAccountData data = context.deserialize(obj.get("data"), AccountData.EncryptedAccountData.class);
                return new Account(username, publicKey, data);
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Account src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("username", src.username().toString());
            obj.addProperty("public_key", Base64.getEncoder().encodeToString(src.publicKey.getEncoded()));
            obj.add("data", context.serialize(src.data));
            return obj;
        }
    }
}
