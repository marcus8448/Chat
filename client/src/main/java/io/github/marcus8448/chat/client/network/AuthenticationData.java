package io.github.marcus8448.chat.client.network;

import io.github.marcus8448.chat.core.network.connection.NetworkConnection;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record AuthenticationData(RSAPrivateKey myPrivateKey, RSAPublicKey serverPublicKey, NetworkConnection connection) {
}
