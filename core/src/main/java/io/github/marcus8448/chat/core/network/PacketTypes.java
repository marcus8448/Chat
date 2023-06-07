package io.github.marcus8448.chat.core.network;

import io.github.marcus8448.chat.core.network.packet.*;

public interface PacketTypes {
    PacketType<ClientHello> CLIENT_HELLO = PacketType.create(ClientHello.class, ClientHello::new);
    PacketType<ClientAuth> CLIENT_AUTH = PacketType.create(ClientAuth.class, ClientAuth::new);
    PacketType<ClientCreateAccount> CLIENT_CREATE_ACCOUNT = PacketType.create(ClientCreateAccount.class, ClientCreateAccount::new);
    PacketType<SendMessage> SEND_MESSAGE = PacketType.create(SendMessage.class, SendMessage::new);
    PacketType<ServerAuthRequest> SERVER_AUTH_REQUEST = PacketType.create(ServerAuthRequest.class, ServerAuthRequest::new);
    PacketType<ServerAuthResponse> SERVER_AUTH_RESPONSE = PacketType.create(ServerAuthResponse.class, ServerAuthResponse::new);

    static void initialize() {}
}
