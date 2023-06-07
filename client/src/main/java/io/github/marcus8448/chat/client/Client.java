package io.github.marcus8448.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

public class Client extends Application {
    private RSAPrivateCrtKey privateKey;
    private RSAPublicKey serverPubKey;
    private Stage primaryStage;

    public Client() {}

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        AnchorPane root = FXMLLoader.load(Objects.requireNonNull(Client.class.getClassLoader().getResource("LoginPrompt.fxml")));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    protected void eventLoop() {
        while (true) {
        }
    }
}
