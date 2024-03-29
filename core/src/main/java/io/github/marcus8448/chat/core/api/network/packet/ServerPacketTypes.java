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

import io.github.marcus8448.chat.core.api.network.packet.common.ChannelList;
import io.github.marcus8448.chat.core.api.network.packet.server.*;

/**
 * Packets that are sent FROM the server TO the client
 */
public interface ServerPacketTypes {
    /**
     * @see AuthenticationRequest
     */
    PacketType<AuthenticationRequest> AUTHENTICATION_REQUEST = PacketType.create(AuthenticationRequest::new);
    /**
     * @see AuthenticationSuccess
     */
    PacketType<AuthenticationSuccess> AUTHENTICATION_SUCCESS = PacketType.create(AuthenticationSuccess::new);
    /**
     * @see AuthenticationFailure
     */
    PacketType<AuthenticationFailure> AUTHENTICATION_FAILURE = PacketType.create(AuthenticationFailure::new);

    /**
     * @see AddMessage
     */
    PacketType<AddMessage> ADD_MESSAGE = PacketType.create(AddMessage::new);
    /**
     * @see SystemMessage
     */
    PacketType<SystemMessage> SYSTEM_MESSAGE = PacketType.create(SystemMessage::new);
    /**
     * @see UserConnect
     */
    PacketType<UserConnect> USER_CONNECT = PacketType.create(UserConnect::new);
    /**
     * @see UserDisconnect
     */
    PacketType<UserDisconnect> USER_DISCONNECT = PacketType.create(UserDisconnect::new);
    /**
     * @see io.github.marcus8448.chat.core.api.network.packet.common.ChannelList
     */
    PacketType<ChannelList> ADD_CHANNELS = PacketType.create(ChannelList::new);
    /**
     * @see io.github.marcus8448.chat.core.api.network.packet.common.ChannelList
     */
    PacketType<ChannelList> REMOVE_CHANNELS = PacketType.create(ChannelList::new);
    PacketType<AddImageMessage> ADD_IMAGE_MESSAGE = PacketType.create(AddImageMessage::new);

    static void initialize() {
    }
}
