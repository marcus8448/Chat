package io.github.marcus8448.chat.client.config;

import com.google.gson.*;
import io.github.marcus8448.chat.core.api.crypto.Encrypted;
import io.github.marcus8448.chat.core.api.crypto.PrivateData;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public record AccountData(RSAPrivateKey privateKey, Map<RSAPublicKey, String> knownAccounts) implements PrivateData<AccountData.EncryptedAccountData> {
    @Override
    public EncryptedAccountData encrypt(Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
        Map<byte[], byte[]> map = new HashMap<>(knownAccounts.size());
        for (Map.Entry<RSAPublicKey, String> entry : this.knownAccounts.entrySet()) {
            map.put(cipher.doFinal(entry.getKey().getEncoded()), cipher.doFinal(entry.getValue().getBytes(StandardCharsets.UTF_8)));
        }
        return new EncryptedAccountData(cipher.doFinal(privateKey.getEncoded()), map);
    }

    public record EncryptedAccountData(byte[] privateKey, Map<byte[], byte[]> knownAccounts) implements Encrypted<AccountData> {
        @Override
        public AccountData decrypt(Cipher cipher) throws IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) CryptoHelper.RSA_KEY_FACTORY.generatePrivate(new PKCS8EncodedKeySpec(cipher.doFinal(privateKey)));
            Map<RSAPublicKey, String> map = new HashMap<>();
            for (Map.Entry<byte[], byte[]> entry : this.knownAccounts.entrySet()) {
                map.put((RSAPublicKey) CryptoHelper.RSA_KEY_FACTORY.generatePublic(new X509EncodedKeySpec(cipher.doFinal(entry.getKey()))), new String(cipher.doFinal(entry.getValue()), StandardCharsets.UTF_8));
            }
            return new AccountData(rsaPrivateKey, map);
        }

        public static class Serializer implements JsonSerializer<EncryptedAccountData>, JsonDeserializer<EncryptedAccountData> {
            public static final Serializer INSTANCE = new Serializer();

            private Serializer() {}
            @Override
            public EncryptedAccountData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                Base64.Decoder decoder = Base64.getDecoder();
                JsonObject obj = json.getAsJsonObject();
                Map<String, JsonElement> map = obj.get("known_accounts").getAsJsonObject().asMap();
                Map<byte[], byte[]> knownUsers = new HashMap<>();
                map.forEach((k, v) -> knownUsers.put(decoder.decode(k), decoder.decode(v.getAsString())));
                return new EncryptedAccountData(decoder.decode(obj.get("private_key").getAsString()), knownUsers);
            }

            @Override
            public JsonElement serialize(EncryptedAccountData src, Type typeOfSrc, JsonSerializationContext context) {
                Base64.Encoder encoder = Base64.getEncoder();
                JsonObject object = new JsonObject();
                JsonObject accounts = new JsonObject();
                src.knownAccounts.forEach((k, v) -> accounts.addProperty(encoder.encodeToString(k), encoder.encodeToString(v)));
                object.addProperty("private_key", encoder.encodeToString(src.privateKey));
                object.add("known_accounts", accounts);
                return object;
            }
        }
    }
}
