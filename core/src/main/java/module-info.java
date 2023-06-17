module chat.core {
    requires org.jetbrains.annotations;
    requires org.apache.logging.log4j;
    exports io.github.marcus8448.chat.core.api;
    exports io.github.marcus8448.chat.core.api.crypto;
    exports io.github.marcus8448.chat.core.api.misc;
    exports io.github.marcus8448.chat.core.api.message;
    exports io.github.marcus8448.chat.core.api.network;
    exports io.github.marcus8448.chat.core.api.network.packet.server;
    exports io.github.marcus8448.chat.core.api.network.packet;
    exports io.github.marcus8448.chat.core.api.account;
    exports io.github.marcus8448.chat.core.api.network.packet.client;
}