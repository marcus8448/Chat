package io.github.marcus8448.chat.core.api.message;

import io.github.marcus8448.chat.core.impl.account.SystemAccount;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.security.interfaces.RSAPublicKey;

public interface MessageAuthor {
    @Contract("_ -> new")
    static @NotNull MessageAuthor system(RSAPublicKey key) {
        return new SystemAccount(key);
    }

    String getFormattedName();

    String getName();

    RSAPublicKey getPublicKey();
}
