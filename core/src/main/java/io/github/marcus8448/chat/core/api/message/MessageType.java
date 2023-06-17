package io.github.marcus8448.chat.core.api.message;

public enum MessageType {
    TEXT(TextMessage.class),
    IMAGE(ImageMessage.class);

    private final Class<? extends Message> subClass;

    MessageType(Class<? extends Message> subClass) {
        this.subClass = subClass;
    }
}
