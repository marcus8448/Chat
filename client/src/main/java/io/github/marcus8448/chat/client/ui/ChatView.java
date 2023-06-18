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

package io.github.marcus8448.chat.client.ui;

import io.github.marcus8448.chat.client.Client;
import io.github.marcus8448.chat.client.ui.cell.MessageCell;
import io.github.marcus8448.chat.client.util.JfxUtil;
import io.github.marcus8448.chat.core.api.message.Message;
import io.github.marcus8448.chat.core.api.network.packet.ClientPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.client.SendMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatView {
    private final Stage stage;
    private final Client client;
    private final Label leftStatus = new Label("...");
    private final Label connectionStatus = new Label("Online");
    private final TextArea messageBox = new TextArea();

    public ChatView(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;
        VBox vBox = new VBox();
        Scene scene = new Scene(vBox, 900, 600);
        vBox.setPrefHeight(600);
        vBox.setPrefWidth(900);
        stage.setWidth(900);
        stage.setHeight(600);
        stage.setTitle("Chat");
        MenuItem preferences = new MenuItem("Preferences");
        preferences.setOnAction(e -> {});
        MenuItem logOut = new MenuItem("Log out");
        logOut.setOnAction(e -> this.client.logout(stage));
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> this.client.shutdown());
        MenuBar bar = new MenuBar(
                new Menu("File", null,
                        preferences,
                        new SeparatorMenuItem(),
                        logOut,
                        exit
                ),
                new Menu("Help", null,
                        new MenuItem("v"),
                        new SeparatorMenuItem(),
                        new MenuItem("d")
                )
        );
        VBox.setVgrow(bar, Priority.NEVER);
        vBox.getChildren().add(bar);

        ObservableList<String> channels = FXCollections.observableArrayList("a", "a", "a", "a");
        ListView<String> leftPane = new ListView<>(channels);

        ListView<Message> centerContent = new ListView<>(this.client.messages);
        this.client.messages.addListener((ListChangeListener<? super Message>) t -> {
            t.next();
            if (t.getAddedSize() > 0) {
                Platform.runLater(() -> {
                    if (this.client.messages.size() > 0) {
                        centerContent.scrollTo(this.client.messages.size() - 1);
                    }
                });
            }
        });
        centerContent.setCellFactory(l -> new MessageCell(centerContent, this.client));
        centerContent.setEditable(true);

        messageBox.setPromptText("Type your message here");
        messageBox.setPrefRowCount(1);
        messageBox.setWrapText(true);
        Button sendButton = new Button();
        HBox messageSendBox = new HBox(messageBox, sendButton);
        messageSendBox.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        sendButton.setMaxHeight(10000);
        sendButton.heightProperty().addListener(o -> {
            sendButton.setPrefWidth(sendButton.getHeight());
            sendButton.setMinWidth(sendButton.getHeight());
        });
        messageBox.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && (e.isShiftDown() || e.isControlDown())) {
                this.sendMessage();
            }
        });
        JfxUtil.buttonPressCallback(sendButton, this::sendMessage);

        HBox.setHgrow(messageBox, Priority.ALWAYS);
        HBox.setHgrow(sendButton, Priority.SOMETIMES);

        VBox centerPane = new VBox(centerContent, messageSendBox);
        VBox.setVgrow(centerContent, Priority.ALWAYS);
        VBox.setVgrow(messageSendBox, Priority.SOMETIMES);

        AnchorPane rightPane = new AnchorPane();

        SplitPane pane = new SplitPane(leftPane, centerPane, rightPane);
        pane.setDividerPositions(0.2, 1.0 - 0.2);
        pane.setFocusTraversable(true);
        VBox.setVgrow(pane, Priority.ALWAYS);
        vBox.getChildren().add(pane);

        HBox.setHgrow(leftStatus, Priority.ALWAYS);
        HBox.setHgrow(connectionStatus, Priority.NEVER);

        Pane pane1 = new Pane();
        HBox.setHgrow(pane1, Priority.ALWAYS);

        HBox hBox = new HBox(leftStatus, pane1, connectionStatus);
        hBox.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(5.0);
        VBox.setVgrow(hBox, Priority.NEVER);
        vBox.getChildren().add(hBox);

        stage.setScene(scene);
        this.client.setView(this);

        stage.setOnCloseRequest(s -> this.client.close());
    }

    private void sendMessage() {
        String message = this.messageBox.getText();
        if (message.isBlank()) {
            return;
        }

        try {
            this.client.connection.send(ClientPacketTypes.SEND_MESSAGE, new SendMessage(message, this.client.signMessage(message)));
            this.messageBox.setText("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
