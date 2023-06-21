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

package io.github.marcus8448.chat.core.api.message;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;

/**
 * A message that contains an image (no text)
 *
 * @param timestamp when the message was received on the server
 * @param author    who sent the message
 * @param width
 * @param height
 * @param image     the image data
 * @param signature the signature of the author, which verifies the message's authenticity
 */
public record ImageMessage(long timestamp, MessageAuthor author, int width, int height, int[] image, byte[] signature) implements Message {
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public MessageAuthor getAuthor() {
        return this.author;
    }

    @Override
    public byte[] getSignature() {
        return this.signature;
    }

    @Override
    public boolean verifySignature()  {
        Signature signature = RSA_SIGNATURE.get(); // get the global RSA signature instance
        try {
            signature.initVerify(this.getAuthor().getPublicKey()); // initialize the instance with the author's key
            byte[] bytes = new byte[this.width * this.height * 4]; // so inefficient, but whatever
            ByteBuffer.wrap(bytes).asIntBuffer().put(this.image);
            signature.update(bytes); // set the data to be the message contents
            return signature.verify(this.getSignature()); // verify the contents
        } catch (InvalidKeyException | SignatureException e) {
            return false; // the verification failed so return false.
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.IMAGE;
    }
}
