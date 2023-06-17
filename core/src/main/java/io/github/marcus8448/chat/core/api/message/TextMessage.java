package io.github.marcus8448.chat.core.api.message;

public interface TextMessage extends Message {
    String getMessage();

    @Override
    default MessageType getType() {
        return MessageType.TEXT;
    }
}
