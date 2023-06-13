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
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.message.Message;
import io.github.marcus8448.chat.core.util.Utils;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
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

        this.editArea.textProperty().addListener((o, old, newStr) -> changed(newStr));
        this.editArea.setWrapText(true);
        this.editArea.setPromptText("Edit message");

        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(e -> this.startEdit());
        MenuItem history = new MenuItem("History...");
        history.setOnAction(e -> {
        });
        ContextMenu contextMenu = new ContextMenu(edit, history);
        this.setContextMenu(contextMenu);
    }

    @Override
    protected void updateItem(Message item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            boolean equals = this.client.getPublicKey().equals(item.author().key());
            this.setEditable(equals);
            this.messageContents.setText(item.contents());
            this.authorName.setText(item.author().usernameAndId());
            this.authorName.setOnMouseClicked(this::openAuthor);
            if (item.verifyChecksum()) {
                this.setBackground(Background.EMPTY);
                this.authorName.setTooltip(new Tooltip(CryptoHelper.sha256Hash(item.author().key().getEncoded())));
            } else {
                this.setBackground(Background.fill(JfxUtil.NOT_VERIFIED_COLOUR));
                this.authorName.setTooltip(new Tooltip("NOT VERIFIED: " + CryptoHelper.sha256Hash(item.author().key().getEncoded())));
            }
        }
    }

    private void openAuthor(MouseEvent mouseEvent) {

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
        if (!isEditing()) return;
        this.editArea.setText(this.getItem().contents());
        this.editArea.setDisable(false);
        this.setGraphic(this.editArea);
    }

    private void changed(String s) {
        int i = 1;
        StringBuilder builder = new StringBuilder("ABC");
        double width = this.editArea.getLayoutBounds().getWidth();
        if (width == 0.0) {
            width = this.hBox.getWidth();
        }
        width -= this.hBox.getPadding().getLeft() + this.hBox.getPadding().getRight();
        System.out.println(width);
        for (char c : s.toCharArray()) {
            if (c == '\n') {
                i++;
                builder.setLength(3);
            } else {
                builder.append(c);
                if (JfxUtil.getTextWidth(builder.toString()) >= width) {
                    i++;
                    builder.setLength(3);
                }
            }
        }
        this.editArea.setPrefRowCount(i);
    }
}
