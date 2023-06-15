package io.github.marcus8448.chat.server.util;

import io.github.marcus8448.chat.core.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Users {
    private final Map<Integer, User> idToUser = new HashMap<>();
    private final Collection<User> users = this.idToUser.values();
    private int current = 0;

    public Users() {

    }

    public boolean contains(RSAPublicKey key) {
        return this.users.stream().anyMatch(u -> u.key().equals(key));
    }

    public @NotNull User createUser(String username, RSAPublicKey key, byte @Nullable [] base64Icon) {
        if (this.contains(key)) throw new UnsupportedOperationException("multiconnect");
        int id = this.current++;
        User user = new User(id, username, key, base64Icon);
        this.idToUser.put(id, user);
        return user;
    }

    public User get(int id) {
        return this.idToUser.get(id);
    }

    public Collection<User> getUsers() {
        return users;
    }
}
