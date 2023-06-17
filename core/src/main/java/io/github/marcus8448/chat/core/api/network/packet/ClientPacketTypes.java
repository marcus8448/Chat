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

package io.github.marcus8448.chat.core.api.network.packet;

import io.github.marcus8448.chat.core.api.network.packet.client.Authenticate;
import io.github.marcus8448.chat.core.api.network.packet.client.Hello;
import io.github.marcus8448.chat.core.api.network.packet.client.SendMessage;

public interface ClientPacketTypes {
    PacketType<Hello> HELLO = PacketType.create(Hello.class, Hello::new);
    PacketType<Authenticate> AUTHENTICATE = PacketType.create(Authenticate.class, Authenticate::new);
    PacketType<SendMessage> SEND_MESSAGE = PacketType.create(SendMessage.class, SendMessage::new);
    PacketType<EmptyRequest> CLIENT_REQUEST_CHANNELS = PacketType.create(EmptyRequest.class, EmptyRequest::new);
    PacketType<EmptyRequest> CLIENT_JOIN_CHANNEL = PacketType.create(EmptyRequest.class, EmptyRequest::new);
    PacketType<EmptyRequest> CLIENT_CREATE_CHANNEL = PacketType.create(EmptyRequest.class, EmptyRequest::new);

    static void initialize() {}
}
