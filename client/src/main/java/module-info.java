module chat.client {
    requires com.google.gson;
    requires org.apache.logging.log4j;
    requires javafx.base;
    requires chat.core;
    requires org.jetbrains.annotations;
    requires javafx.graphics;
    requires javafx.controls;

    exports io.github.marcus8448.chat.client;
    exports io.github.marcus8448.chat.client.config;
}