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
import io.github.marcus8448.chat.client.ui.cell.UserCell;
import io.github.marcus8448.chat.client.util.JfxUtil;
import io.github.marcus8448.chat.core.api.account.User;
import io.github.marcus8448.chat.core.api.message.Message;
import io.github.marcus8448.chat.core.api.network.packet.ClientPacketTypes;
import io.github.marcus8448.chat.core.api.network.packet.client.SendMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main chat window.
 */
public class ChatView {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * The active (probably primary) stage
     */
    private final Stage stage;
    /**
     * The client instance
     */
    private final Client client;
    /**
     * The status display on the left side
     */
    private final Label leftStatus = new Label("...");
    /**
     * The status display on the right side
     * Displays online/offline base don connection state
     */
    private final Label connectionStatus = new Label("Online");
    /**
     * Area for the user to type messages for sending
     */
    private final TextArea messageBox = new TextArea();
    /**
     * Button that sends messages
     * ctrl-enter also works instead
     */
    private final Button sendButton = new Button();
    /**
     * Left panel - list of channels
     */
    private final ListView<String> channelsList;
    /**
     * Center/main content pane - messages
     */
    private final ListView<Message> messagesList;
    /**
     * Right pane - list of users
     */
    private final ListView<User> userList;

    public ChatView(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;
        VBox vBox = new VBox(); // root pane

        //setup scene/stage
        Scene scene = new Scene(vBox);
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        stage.setWidth(bounds.getWidth() / 4.0 * 3.0);
        stage.setHeight(bounds.getHeight() / 4.0 * 3.0);
        stage.setTitle("Chat");
        stage.centerOnScreen();

        // create top toolbar
        MenuItem preferences = new MenuItem("Preferences"); //TODO
        preferences.setOnAction(e -> {
        });
        MenuItem logOut = new MenuItem("Log out");
        logOut.setOnAction(e -> this.client.logout(stage)); // logs out the user (to login screen)
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> this.client.shutdown()); // closes the app
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(e -> this.client.openEditSelfScreen());  // edit username
        MenuItem trust = new MenuItem("Trust");
        trust.setOnAction(e -> this.client.openTrustScreen(null)); // edit user trust
        MenuBar bar = new MenuBar( //create the bar
                new Menu("File", null,
                        preferences,
                        new SeparatorMenuItem(),
                        logOut,
                        exit
                ),
                new Menu("Account", null,
                        edit,
                        new SeparatorMenuItem(),
                        trust
                )
        );
        VBox.setVgrow(bar, Priority.NEVER); // disable growth of the bar
        vBox.getChildren().add(bar); // add the bar to the window

        ObservableList<String> channels = FXCollections.observableArrayList("a", "a", "a", "a"); //fixme
        channelsList = new ListView<>(channels); // create the list of channels

        messagesList = new ListView<>(this.client.messages); // create the messages pane
        this.client.messages.addListener((ListChangeListener<? super Message>) t -> { // autoscroll to bottom
            t.next();
            if (t.getAddedSize() > 0) {
                Platform.runLater(() -> {
                    if (this.client.messages.size() > 0) {
                        messagesList.scrollTo(this.client.messages.size() - 1);
                    }
                });
            }
        });
        messagesList.setCellFactory(l -> new MessageCell(messagesList, this.client)); // see message cell

        // create and setup the input area
        messageBox.setPromptText("Type your message here");
        messageBox.setPrefRowCount(1);
        messageBox.setWrapText(true);
        HBox messageSendBox = new HBox(messageBox, sendButton);
        messageSendBox.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        sendButton.setMaxHeight(10000);
        sendButton.heightProperty().addListener(o -> { // make button square
            sendButton.setPrefWidth(sendButton.getHeight());
            sendButton.setMinWidth(sendButton.getHeight());
        });
        messageBox.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && (e.isControlDown())) { // check if ctrl-enter is pressed
                this.sendMessage(); // send message
            }
        });
        JfxUtil.buttonPressCallback(sendButton, this::sendMessage); // send message on button press

        // set growth properties
        HBox.setHgrow(messageBox, Priority.ALWAYS);
        HBox.setHgrow(sendButton, Priority.SOMETIMES);
        VBox.setVgrow(messagesList, Priority.ALWAYS);
        VBox.setVgrow(messageSendBox, Priority.SOMETIMES);

        VBox centerPane = new VBox(messagesList, messageSendBox); // create center pane

        userList = new ListView<>(this.client.userList); // create right panel (users)
        userList.setCellFactory(c -> new UserCell(this.client)); // see user cell

        SplitPane pane = new SplitPane(channelsList, centerPane, userList); // Create main content pane
        pane.setDividerPositions(0.10, 1.0 - 0.10); // allocate space: 10%, 80%, 10%
        pane.setFocusTraversable(true);
        vBox.getChildren().add(pane);

        VBox.setVgrow(pane, Priority.ALWAYS); // set growth properties
        HBox.setHgrow(leftStatus, Priority.ALWAYS);
        HBox.setHgrow(connectionStatus, Priority.NEVER);

        Pane spacing = new Pane(); // padding
        HBox.setHgrow(spacing, Priority.ALWAYS);

        HBox statuses = new HBox(leftStatus, spacing, connectionStatus); // status bar
        statuses.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        statuses.setAlignment(Pos.CENTER_LEFT);
        statuses.setSpacing(5.0);
        VBox.setVgrow(statuses, Priority.NEVER);
        vBox.getChildren().add(statuses);

        stage.setScene(scene); // set the scene
        this.client.setView(this); // inform the client of our existence

        stage.setOnCloseRequest(s -> {
            LOGGER.info("Closing app...");
            this.client.close();
        }); // when the app is closed, shutdown the entire client
    }

    /**
     * Sends the message written in the messageBox
     */
    private void sendMessage() {
        if (this.sendButton.isDisabled()) return; // if we can't send, don't bother
        String message = this.messageBox.getText();
        if (message.isBlank()) {
            return;
        }

        try {
            this.client.connection.send(ClientPacketTypes.SEND_MESSAGE, new SendMessage(message, this.client.signMessage(message)));
            this.messageBox.setText("");
        } catch (Exception e) {
            LOGGER.fatal("Failed to send message", e);
        }
    }

    /**
     * Set status to offline
     */
    public void markOffline() {
        this.sendButton.setDisable(true); // disable message sending
        this.connectionStatus.setText("OFFLINE");
    }

    /**
     * Set status to online
     */
    public void markOnline() {
        this.sendButton.setDisable(false); // enable message sending
        this.connectionStatus.setText("Online");
    }

    /**
     * Redraw the main content pane (for username updates, etc.)
     */
    public void refresh() {
        this.channelsList.refresh();
        this.userList.refresh();
        this.messagesList.refresh();
    }
}
