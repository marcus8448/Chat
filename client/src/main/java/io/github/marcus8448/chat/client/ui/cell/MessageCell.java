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

package io.github.marcus8448.chat.client.ui.cell;

import io.github.marcus8448.chat.client.Client;
import io.github.marcus8448.chat.client.util.JfxUtil;
import io.github.marcus8448.chat.core.message.Message;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class MessageCell extends ListCell<Message> {
    private final Circle authorPicture = new Circle();
    private final Label authorName = new Label();
    private final Label messageContents = new Label();
    private final VBox vBox = new VBox(authorName, messageContents);
    private final HBox hBox = new HBox(authorPicture, vBox);
    private final TextArea editArea = new TextArea();
    private final Client client;

    public MessageCell(Client client) {
        super();
        this.client = client;
        this.setEditable(false);
        HBox.setHgrow(authorPicture, Priority.NEVER);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        VBox.setVgrow(authorName, Priority.NEVER);
        VBox.setVgrow(messageContents, Priority.ALWAYS);
        this.setGraphic(hBox);
        this.setText(null);

        this.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ENTER) { //only passed if ctrl is pressed
                Message item = this.getItem();
                String text = this.editArea.getText();
                this.commitEdit(new Message(item.timestamp(), item.author(), client.signMessage(text), text));
            }
        });

        this.editArea.textProperty().addListener((o, old, newStr) -> {
            int i = 0;
            StringBuilder builder = new StringBuilder();
            for (char c : newStr.toCharArray()) {
                if (c == '\n') {
                    i++;
                    builder.setLength(0);
                } else {
                    builder.append(c);
                    if (JfxUtil.getTextWidth(builder.toString()) >= this.editArea.getLayoutBounds().getWidth()) {
                        i++;
                        builder.setLength(0);
                    }
                }
            }
            this.editArea.setPrefRowCount(i);
        });
        this.editArea.setWrapText(true);
        this.editArea.setPrefRowCount(1);
        this.editArea.setPromptText("Edit message");
    }

    private byte[] signEdit() {
        return null;
    }

    @Override
    protected void updateItem(Message item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(null);
        if (!empty && item != null) {
            boolean equals = client.getPublicKey().equals(item.author().key());
            System.out.println("EQ");
            this.setEditable(equals);
            this.messageContents.setText(item.contents());
            this.authorName.setText(item.author().usernameAndId());
        }
    }

    @Override
    public void commitEdit(Message newValue) {
        super.commitEdit(newValue);
        this.editArea.setDisable(true);
        this.setGraphic(this.hBox);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        this.editArea.setDisable(true);
        this.setGraphic(this.hBox);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        System.out.println("STEDIT");
        if (!isEditing()) return;
        this.setGraphic(this.editArea);
        this.editArea.setDisable(false);
        this.editArea.setText(this.getItem().contents());
    }
}
