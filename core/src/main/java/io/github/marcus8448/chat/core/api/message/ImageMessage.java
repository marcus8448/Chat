package io.github.marcus8448.chat.core.api.message;

public record ImageMessage(long timestamp, MessageAuthor author, byte[] image, byte[] signature) implements Message {
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
        return this.image;
    }

    @Override
    public byte[] getSignature() {
        return this.signature;
    }

    @Override
    public MessageType getType() {
        return MessageType.IMAGE;
    }
}
