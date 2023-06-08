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

import io.github.marcus8448.chat.core.Result;
import io.github.marcus8448.chat.core.api.connection.BinaryInput;
import io.github.marcus8448.chat.core.api.connection.BinaryOutput;
import io.github.marcus8448.chat.core.network.NetworkedData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ServerAuthResponse implements NetworkedData {
    private final Result<Void, String> result;

    public ServerAuthResponse(BinaryInput input) throws IOException {
        if (input.readBoolean()) {
            this.result = Result.ok(null);
        } else {
            this.result = Result.error(input.readString());
        }
    }

    @Contract("true, !null -> fail; false, null -> fail")
    public ServerAuthResponse(boolean success, @Nullable String failureReason) {
        if (success) {
            assert failureReason == null;
            this.result = Result.ok(null);
        } else {
            assert failureReason != null;
            this.result = Result.error(failureReason);
        }
    }

    public ServerAuthResponse(@NotNull String failureReason) {
        this.result = Result.error(failureReason);
    }

    @Override
    public void write(BinaryOutput output) throws IOException {
        output.writeBoolean(result.isOk());
        if (result.isError()) {
            output.writeString(result.unwrapError());
        }
    }

    public boolean isSuccess() {
        return this.result.isOk();
    }

    public String getFailureReason() {
        return this.result.unwrapError();
    }
}
