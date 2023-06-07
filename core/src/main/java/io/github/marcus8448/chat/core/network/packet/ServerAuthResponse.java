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

package io.github.marcus8448.chat.core.network.packet;

import io.github.marcus8448.chat.core.network.NetworkedData;
import io.github.marcus8448.chat.core.network.connection.ConnectionInput;
import io.github.marcus8448.chat.core.network.connection.ConnectionOutput;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ServerAuthResponse implements NetworkedData {
    boolean success;
    @Nullable String failureReason;

    public ServerAuthResponse() {}

    @Contract("true, !null -> fail; false, null -> fail")
    public ServerAuthResponse(boolean success, @Nullable String failureReason) {
        if (success) assert failureReason == null;
        else assert failureReason != null;

        this.success = success;
        this.failureReason = failureReason;
    }

    public ServerAuthResponse(@NotNull String failureReason) {
        this.success = false;
        this.failureReason = failureReason;
    }

    @Override
    public void write(ConnectionOutput output) throws IOException {
        output.write(success ? 1 : 0);
        if (!success) {
            output.writeString(failureReason);
        }
    }

    @Override
    public void read(ConnectionInput input) throws IOException {
        success = input.read() == 1;
        if (!success) {
            failureReason = input.readString();
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
