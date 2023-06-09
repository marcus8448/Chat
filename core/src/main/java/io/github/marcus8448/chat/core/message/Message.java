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

package io.github.marcus8448.chat.core.message;

import io.github.marcus8448.chat.core.api.crypto.CryptoConstants;
import io.github.marcus8448.chat.core.user.User;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

public record Message(long timestamp, User author, byte[] checksum, String contents) {
    public static Message create(long timestamp, User author, PrivateKey privateKey, String contents) {
        Signature rsaSignature = CryptoConstants.getRsaSignature();
        byte[] checksum;
        try {
            rsaSignature.initSign(privateKey);
            rsaSignature.update(contents.getBytes(StandardCharsets.UTF_8));
            checksum = rsaSignature.sign();
        } catch (InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
        return new Message(timestamp, author, checksum, contents);
    }

    public boolean verifyChecksum() {
        Signature rsaSignature = CryptoConstants.getRsaSignature();
        try {
            rsaSignature.initVerify(this.author.key());
            rsaSignature.update(this.contents.getBytes(StandardCharsets.UTF_8));
            return rsaSignature.verify(this.checksum);
        } catch (InvalidKeyException | SignatureException e) {
            return false;
        }
    }
}
