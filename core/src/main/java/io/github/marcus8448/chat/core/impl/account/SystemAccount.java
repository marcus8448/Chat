package io.github.marcus8448.chat.core.impl.account;

import io.github.marcus8448.chat.core.api.message.MessageAuthor;

import java.security.interfaces.RSAPublicKey;

public record SystemAccount(RSAPublicKey publicKey) implements MessageAuthor {
    @Override
    public String getFormattedName() {
        return "SYSTEM";
    }

    @Override
    public String getName() {
        return "SYSTEM";
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return this.publicKey;
    }
}
