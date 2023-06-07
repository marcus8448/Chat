package io.github.marcus8448.chat.client.ui;

import io.github.marcus8448.chat.client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ChatView {
    private final Stage stage;
    private final Client client;

    public ChatView(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;
        VBox vBox = new VBox();
        Scene scene = new Scene(vBox, 900, 600);
        vBox.setPrefHeight(600);
        vBox.setPrefWidth(900);
        MenuBar bar = new MenuBar(
                new Menu("Menu", null,
                        new Menu("a"),
                        new SeparatorMenuItem(),
                        new Menu("b")
                ),
                new Menu("2", null,
                        new Menu("v"),
                        new SeparatorMenuItem(),
                        new Menu("d")
                )
        );
        VBox.setVgrow(bar, Priority.NEVER);
        vBox.getChildren().add(bar);

        ObservableList<String> channels = FXCollections.observableArrayList("a", "a", "a", "a");
        ListView<String> leftPane = new ListView<>(channels);

        ObservableList<String> messages = FXCollections.observableArrayList("a", "a", "a", "a");
        ListView<String> centerContent = new ListView<>(messages);

        TextArea messageBox = new TextArea("Message");
        messageBox.setPrefRowCount(1);
        Button sendButton = new Button();
        HBox messageSendBox = new HBox(messageBox, sendButton);
        messageSendBox.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        sendButton.setMaxHeight(10000);
        sendButton.setPrefWidth(sendButton.getHeight());
        HBox.setHgrow(messageBox, Priority.ALWAYS);
        HBox.setHgrow(sendButton, Priority.NEVER);

        VBox centerPane = new VBox(centerContent, messageSendBox);
        VBox.setVgrow(centerContent, Priority.ALWAYS);
        VBox.setVgrow(messageSendBox, Priority.SOMETIMES);

        AnchorPane rightPane = new AnchorPane();

        SplitPane pane = new SplitPane(leftPane, centerPane, rightPane);
        pane.setDividerPositions(0.2, 1.0 - 0.2);
        pane.setFocusTraversable(true);
        VBox.setVgrow(pane, Priority.ALWAYS);
        vBox.getChildren().add(pane);

        Label leftStatus = new Label("LEFT");
        HBox.setHgrow(leftStatus, Priority.ALWAYS);

        Pane pane1 = new Pane();
        HBox.setHgrow(pane1, Priority.ALWAYS);

        Label rightStatus = new Label("RIGHT");
        HBox.setHgrow(rightStatus, Priority.NEVER);

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(5.0);
        VBox.setVgrow(hBox, Priority.NEVER);
        vBox.getChildren().add(hBox);

        stage.setScene(scene);
        stage.show();
    }
}
