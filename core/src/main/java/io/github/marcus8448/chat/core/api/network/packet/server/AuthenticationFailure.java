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

package io.github.marcus8448.chat.core.api.network.packet.server;

import io.github.marcus8448.chat.core.api.network.NetworkedData;
import io.github.marcus8448.chat.core.api.network.io.BinaryInput;
import io.github.marcus8448.chat.core.api.network.io.BinaryOutput;
import io.github.marcus8448.chat.core.api.network.packet.client.Authenticate;

import java.io.IOException;

/**
 * Represents a client's failure to meet the server's authentication challenge
 * @see Authenticate
 */
public class AuthenticationFailure implements NetworkedData {
    private final String reason;

    public AuthenticationFailure(BinaryInput input) throws IOException {
        this.reason = input.readString();
    }

    public AuthenticationFailure(String reason) {
        this.reason = reason;
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeString(this.reason);
    }

    public String getReason() {
        return reason;
    }
}
