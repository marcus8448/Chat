package io.github.marcus8448.chat.client.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuthenticationResult {
    private final boolean success;
    private final @Nullable String reason;

    private AuthenticationResult(boolean success, @Nullable String reason) {
        this.success = success;
        this.reason = reason;
    }

    public static AuthenticationResult success() {
        return new AuthenticationResult(true, null);
    }

    public static AuthenticationResult failure(@NotNull String reason) {
        return new AuthenticationResult(false, reason);
    }

    public String getReason() {
        return reason;
    }

    public boolean isSuccess() {
        return success;
    }
}
