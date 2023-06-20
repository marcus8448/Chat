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
import io.github.marcus8448.chat.client.parse.MarkdownParser;
import io.github.marcus8448.chat.client.util.JfxUtil;
import io.github.marcus8448.chat.core.api.crypto.CryptoHelper;
import io.github.marcus8448.chat.core.api.message.Message;
import io.github.marcus8448.chat.core.api.message.MessageType;
import io.github.marcus8448.chat.core.api.message.TextMessage;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextFlow;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class MessageCell extends ListCell<Message> {
    private final Background NOT_VERIFIED_BG = Background.fill(JfxUtil.NOT_VERIFIED_COLOUR);

    private final Circle authorPicture = new Circle();
    private final Label authorName = new Label();
    private final Pane imageContents = new Pane();
    private final VBox vBox = new VBox(authorName);
    private final HBox hBox = new HBox(authorPicture, vBox);
    private final TextArea editArea = new TextArea();
    private final ContextMenu contextMenu;
    private final Client client;
    private final TextFlow textMessageContents = new TextFlow();

    public MessageCell(ListView<Message> centerContent, Client client) {
        super();
        this.client = client;
        this.setEditable(false);
        HBox.setHgrow(authorPicture, Priority.NEVER);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        VBox.setVgrow(authorName, Priority.NEVER);
        VBox.setVgrow(textMessageContents, Priority.ALWAYS);
        this.setGraphic(hBox);
        this.setText(null);

        this.textMessageContents.prefWidthProperty().bind(centerContent.widthProperty().subtract(16.0));

        this.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ENTER) { //only passed if ctrl is pressed
                Message item = this.getItem();
                String text = this.editArea.getText();
//                this.commitEdit(new Message(item.getType(), item.author(), client.signMessage(text), text)); //todo: editing
            }
        });

        this.editArea.textProperty().addListener((o, old, newStr) -> changed(newStr));
        this.editArea.setWrapText(true);
        this.editArea.setPromptText("Edit message");

//        MenuItem edit = new MenuItem("Edit");
//        edit.setOnAction(e -> this.startEdit());
        MenuItem copyContents = new MenuItem("Copy contents");
        copyContents.setOnAction(e -> this.copyContents());
        MenuItem copyAuthorName = new MenuItem("Copy author name");
        copyAuthorName.setOnAction(e -> this.copyAuthorName());
        MenuItem copyAuthorId = new MenuItem("Copy author ID");
        copyAuthorId.setOnAction(e -> this.copyAuthorId());
//        MenuItem history = new MenuItem("History...");
//        history.setOnAction(e -> {
//        });
        this.contextMenu = new ContextMenu(/*edit, history, */copyAuthorName, copyContents, copyAuthorId);
    }

    private void copyAuthorId() {
        Map<DataFormat, Object> data = new HashMap<>();
        data.put(DataFormat.PLAIN_TEXT, CryptoHelper.sha256Hash(this.getItem().getAuthor().getPublicKey().getEncoded()));
        Clipboard.getSystemClipboard().setContent(data);
    }

    private void copyAuthorName() {
        Map<DataFormat, Object> data = new HashMap<>();
        data.put(DataFormat.PLAIN_TEXT, this.client.getName(this.getItem().getAuthor()));
        Clipboard.getSystemClipboard().setContent(data);
    }

    private void copyContents() {
        Map<DataFormat, Object> data = new HashMap<>();
        if (this.getItem().getType() == MessageType.TEXT) {
            data.put(DataFormat.PLAIN_TEXT, ((TextMessage) this.getItem()).getMessage());
        } else {
            data.put(DataFormat.IMAGE, this.getItem().getContents()); //fixme?
        }
        Clipboard.getSystemClipboard().setContent(data);
    }

    @Override
    protected void updateItem(Message item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
//            boolean canEdit = this.client.getPublicKey().equals(item.getAuthor().getPublicKey()) && item instanceof TextMessage; //todo: editing
//            this.setEditable(canEdit);
            this.authorName.setBackground(Background.EMPTY);
            if (item.getType() == MessageType.TEXT) {
                this.vBox.getChildren().remove(this.imageContents);
                if (!this.vBox.getChildren().contains(this.textMessageContents)) this.vBox.getChildren().add(this.textMessageContents);
                MarkdownParser.parseMarkdown(this.textMessageContents, ((TextMessage) item).getMessage());
            } else if (item.getType() == MessageType.IMAGE) {
                this.vBox.getChildren().remove(this.textMessageContents);
                if (!this.vBox.getChildren().contains(this.imageContents)) this.vBox.getChildren().add(this.imageContents);
                Image image = new Image(new ByteArrayInputStream(item.getContents()));
                this.imageContents.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
                this.textMessageContents.getChildren().clear();
            }
            this.authorName.setText(this.client.getName(item.getAuthor()));
            this.authorName.setOnMouseClicked(this::openAuthor);
            this.setContextMenu(contextMenu);
            String hash = CryptoHelper.sha256Hash(item.getAuthor().getPublicKey().getEncoded());
            if (item.verifySignature()) {
                if (this.client.isTrusted(item.getAuthor())) {
                    this.authorName.setTooltip(new Tooltip(item.getAuthor().getFormattedName()));
                } else {
                    this.authorName.setTooltip(new Tooltip(hash));
                }
            } else {
                this.authorName.setBackground(NOT_VERIFIED_BG);
                this.authorName.setTooltip(new Tooltip("NOT VERIFIED: " + hash));
            }
        } else {
            this.authorName.setBackground(Background.EMPTY);
            this.authorName.setText("");
            this.authorName.setOnMouseClicked(null);
            this.vBox.getChildren().remove(this.imageContents);
            this.vBox.getChildren().remove(this.textMessageContents);
            this.imageContents.setBackground(Background.EMPTY);

            this.setContextMenu(null);
        }
    }

    private void openAuthor(MouseEvent mouseEvent) {

    }

    @Override
    public void commitEdit(Message newValue) {
        super.commitEdit(newValue);
        this.editArea.setDisable(true);
        this.setGraphic(this.hBox);
        this.setText(null);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        this.editArea.setDisable(true);
        this.setGraphic(this.hBox);
        this.setText(null);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (!isEditing()) return;
        this.editArea.setText(((TextMessage) this.getItem()).getMessage());
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
