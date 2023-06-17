package io.github.marcus8448.chat.core.impl.message;

import io.github.marcus8448.chat.core.api.message.MessageAuthor;
import io.github.marcus8448.chat.core.api.message.TextMessage;

import java.nio.charset.StandardCharsets;

public record InsecureTextMessage(long timestamp, MessageAuthor author, String contents) implements TextMessage {
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public MessageAuthor getAuthor() {
        return this.author;
    }

    @Override
    public byte[] getContents() {
        return this.contents.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getSignature() {
        return new byte[0];
    }

    @Override
    public String getMessage() {
        return this.contents;
    }

    @Override
    public boolean verifySignature() {
        return false;
    }
}
