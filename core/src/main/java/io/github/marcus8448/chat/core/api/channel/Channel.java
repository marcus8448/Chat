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

package io.github.marcus8448.chat.core.api.channel;

import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.misc.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Channel {
    private final Identifier id;
    private final List<User> participants = new ArrayList<>();

    public Channel(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public void addParticipant(User user) {
        this.participants.add(user);
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
    }

    public List<User> getParticipants() {
        return participants;
    }

    public boolean contains(User user) {
        return this.participants.contains(user);
    }
}
