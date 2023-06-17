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

import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.user.User;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Represents a message
 * @param timestamp the time the message was received on the server
 * @param author the entity that created the message
 * @param checksum the signature of the creator, verifying the contents
 * @param contents the contents of the message
 */
public record Message(long timestamp, User author, byte[] checksum, String contents) {
    private static final ThreadLocal<Signature> RSA_SIGNATURE = ThreadLocal.withInitial(CryptoHelper::createRsaSignature);

    /**
     * Verifies that the message checksum matches the contents (message was actually authored by the author)
     * @return whether the message checksum is valid
     */
    public boolean verifyChecksum() {
        Signature signature = RSA_SIGNATURE.get(); // get the global RSA signature instance
        try {
            signature.initVerify(this.author.key()); // initialize the instance with the author's key
            signature.update(this.contents.getBytes(StandardCharsets.UTF_8)); // set the data to be the message contents
            return signature.verify(this.checksum); // verify the contents
        } catch (InvalidKeyException | SignatureException e) {
            return false; // the verification failed so return false.
        }
    }
}
