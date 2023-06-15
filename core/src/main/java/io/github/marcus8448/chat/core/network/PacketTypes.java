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

package io.github.marcus8448.chat.core.network;

import io.github.marcus8448.chat.core.network.packet.*;

public interface PacketTypes {
    PacketType<ClientHello> CLIENT_HELLO = PacketType.create(ClientHello.class, ClientHello::new);
    PacketType<ClientAuthResponse> CLIENT_AUTH = PacketType.create(ClientAuthResponse.class, ClientAuthResponse::new);
    PacketType<ServerAuthRequest> SERVER_AUTH_REQUEST = PacketType.create(ServerAuthRequest.class, ServerAuthRequest::new);
    PacketType<ServerAuthResponse> SERVER_AUTH_RESPONSE = PacketType.create(ServerAuthResponse.class, ServerAuthResponse::new);

    PacketType<NewMessage> NEW_MESSAGE = PacketType.create(NewMessage.class, NewMessage::new);

    PacketType<SendMessage> SEND_MESSAGE = PacketType.create(SendMessage.class, SendMessage::new);
    PacketType<EmptyRequest> CLIENT_REQUEST_ONLINE = PacketType.create(EmptyRequest.class, EmptyRequest::new);
    PacketType<EmptyRequest> CLIENT_REQUEST_CHANNELS = PacketType.create(EmptyRequest.class, EmptyRequest::new);
    PacketType<EmptyRequest> CLIENT_JOIN_CHANNEL = PacketType.create(EmptyRequest.class, EmptyRequest::new);
    PacketType<EmptyRequest> CLIENT_CREATE_CHANNEL = PacketType.create(EmptyRequest.class, EmptyRequest::new);

    static void initialize() {}
}
