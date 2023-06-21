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
import io.github.marcus8448.chat.core.api.network.packet.client.SendImageMessage;
import io.github.marcus8448.chat.core.api.network.packet.client.SendMessage;
import io.github.marcus8448.chat.core.api.network.packet.common.ChannelList;

/**
 * Types of packets SENT by the client TO the server
 */
public interface ClientPacketTypes {
    /**
     * @see Hello
     */
    PacketType<Hello> HELLO = PacketType.create(Hello.class, Hello::new);
    /**
     * @see Authenticate
     */
    PacketType<Authenticate> AUTHENTICATE = PacketType.create(Authenticate.class, Authenticate::new);
    /**
     * @see SendMessage
     */
    PacketType<SendMessage> SEND_MESSAGE = PacketType.create(SendMessage.class, SendMessage::new);
    PacketType<SendImageMessage> SEND_IMAGE_MESSAGE = PacketType.create(SendImageMessage.class, SendImageMessage::new);

    /**
     * @see ChannelList
     */
    PacketType<ChannelList> JOIN_CHANNELS = PacketType.create(ChannelList.class, ChannelList::new);
    /**
     * @see ChannelList
     */
    PacketType<ChannelList> LEAVE_CHANNELS = PacketType.create(ChannelList.class, ChannelList::new);

    static void initialize() {
    }
}
