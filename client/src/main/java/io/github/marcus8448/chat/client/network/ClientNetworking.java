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

package io.github.marcus8448.chat.client.network;

import io.github.marcus8448.chat.core.api.connection.PacketPipeline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientNetworking {
    public static PacketPipeline connect(InetSocketAddress address) throws IOException {
        Socket socket = new Socket();
        socket.bind(null);
        socket.connect(address);
        return PacketPipeline.createNetworked(socket);
    }
}
