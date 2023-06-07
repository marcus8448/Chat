package io.github.marcus8448.chat.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.marcus8448.chat.client.ui.LoginScreen;
import javafx.application.Application;
import javafx.stage.Stage;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

public class Client extends Application {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private RSAPrivateCrtKey privateKey;
    private RSAPublicKey serverPubKey;
    private Stage primaryStage;

    public Client() {}

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        LoginScreen chatView = new LoginScreen(this, primaryStage);

//        Pair<AnchorPane, LoginPrompt> load = JfxUtil.load(LoginPrompt.class, "LoginPrompt.fxml");
//        Scene scene = new Scene(load.left());
//        primaryStage.setTitle("Login");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//
//        System.out.println("DONE");
    }

    protected void eventLoop() {
        while (true) {
        }
    }
}
