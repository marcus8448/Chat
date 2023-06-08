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

package io.github.marcus8448.chat.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.marcus8448.chat.client.config.Account;
import io.github.marcus8448.chat.client.ui.LoginScreen;
import javafx.application.Application;
import javafx.stage.Stage;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public List<Account> accounts = new ArrayList<>();

    private RSAPrivateCrtKey privateKey;
    private RSAPublicKey serverPubKey;
    private Stage primaryStage;

    public Client() {}

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        LoginScreen chatView = new LoginScreen(this, primaryStage);
        primaryStage.show();

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
