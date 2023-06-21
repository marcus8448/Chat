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

package io.github.marcus8448.chat.server.util;

import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.misc.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Users {
    private final Map<Integer, User> idToUser = new HashMap<>();
    private final Collection<User> users = this.idToUser.values();
    private int current = 0;

    public Users() {

    }

    public boolean contains(RSAPublicKey key) {
        // any user with the same key
        return this.users.stream().anyMatch(u -> u.key().equals(key));
    }

    public @NotNull User createUser(Identifier username, RSAPublicKey key, byte @Nullable [] base64Icon) {
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

    public void remove(User user) {
        User remove = this.idToUser.remove(user.sessionId());
        assert remove == user;
    }

    public boolean canAccept(RSAPublicKey key) {
        return !this.contains(key);
    }
}
