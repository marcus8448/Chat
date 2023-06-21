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
import io.github.marcus8448.chat.core.api.message.ImageMessage;
import io.github.marcus8448.chat.core.api.message.Message;
import io.github.marcus8448.chat.core.api.message.MessageType;
import io.github.marcus8448.chat.core.api.message.TextMessage;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Graphical representation of a message
 */
public class MessageCell extends ListCell<Message> {
    /**
     * Background to use when a message signature is invalid
     */
    private static final Background NOT_VERIFIED_BG = Background.fill(JfxUtil.NOT_VERIFIED_COLOUR);

    private static final Font AUTHOR_FONT = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, FontPosture.REGULAR, Font.getDefault().getSize() + 2);

    /**
     * Circular picture of message author
     */
    private final Circle authorPicture = new Circle();
    /**
     * Label with the name of the author
     */
    private final Label authorName = new Label();
    /**
     * Image pane (if the message is an image message)
     */
    private final Pane imageContents = new Pane();
    /**
     * The main/message content panel
     */
    private final VBox vBox = new VBox(authorName);
    /**
     * Context menu for non-blank messages
     */
    private final ContextMenu contextMenu;
    /**
     * The client instance
     */
    private final Client client;
    /**
     * The contents of the message (if the image is a text message)
     */
    private final TextFlow textMessageContents = new TextFlow();

    public MessageCell(ListView<Message> centerContent, Client client) {
        super();
        this.client = client;
        this.setEditable(false);
        // set growth priorities
        HBox.setHgrow(authorPicture, Priority.NEVER);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        VBox.setVgrow(authorName, Priority.NEVER);
        VBox.setVgrow(textMessageContents, Priority.ALWAYS);
        HBox hBox = new HBox(authorPicture, vBox); // create cell content box
        this.setGraphic(hBox); // set the contents
        this.setText(null); // we don't want the default text flow

        this.authorName.setFont(AUTHOR_FONT);

        // lock the width of the contents to the width of the available space
        this.textMessageContents.prefWidthProperty().bind(centerContent.widthProperty().subtract(20.0));

        // create the contxt menu
        MenuItem copyContents = new MenuItem("Copy contents");
        copyContents.setOnAction(e -> this.copyContents()); // copies message contents
        MenuItem copyAuthorName = new MenuItem("Copy author name");
        copyAuthorName.setOnAction(e -> this.copyAuthorName()); // copies the name of the author
        MenuItem copyAuthorId = new MenuItem("Copy author ID");
        copyAuthorId.setOnAction(e -> this.copyAuthorId()); // copies the public key SHA-256 of the author
        this.contextMenu = new ContextMenu(copyAuthorName, copyContents, copyAuthorId); // create the menu
    }

    @Override
    protected void updateItem(Message item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) { // check if there is any item
            this.authorName.setBackground(Background.EMPTY); // reset signature verification background
            if (item.getType() == MessageType.TEXT) { // check if this is a text message
                this.vBox.getChildren().remove(this.imageContents); // remove image contents (if exists)
                //add text message contents if it doesn't already exist
                if (!this.vBox.getChildren().contains(this.textMessageContents))
                    this.vBox.getChildren().add(this.textMessageContents);
                // parse the text as markdown and insert it into the flow
                MarkdownParser.parseMarkdown(this.textMessageContents, ((TextMessage) item).getMessage());
            } else if (item.getType() == MessageType.IMAGE) {
                this.vBox.getChildren().remove(this.textMessageContents); // remove text message contents from display
                // delete any remaining text contents
                this.textMessageContents.getChildren().clear();
                // add image content if it doesn't already exist
                if (!this.vBox.getChildren().contains(this.imageContents))
                    this.vBox.getChildren().add(this.imageContents);
                Image image = new WritableImage(new PixelBuffer<>(((ImageMessage) item).width(), ((ImageMessage) item).height(), IntBuffer.wrap(((ImageMessage) item).image()), PixelFormat.getIntArgbPreInstance())); // load the image
                // set the image data
                this.imageContents.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
                this.imageContents.setPrefWidth(image.getWidth());
                this.imageContents.setPrefHeight(image.getHeight());
            }
            this.authorName.setText(this.client.getName(item.getAuthor())); // set the author text
            this.authorName.setOnMouseClicked(this::openAuthor); // set click handler
            this.setContextMenu(contextMenu); // set the content menu (since active)
            String hash = CryptoHelper.sha256Hash(item.getAuthor().getPublicKey().getEncoded()); //get the key id of the author
            if (item.verifySignature()) { // check that the message signature is valid
                if (this.client.isTrusted(item.getAuthor())) { // check if the author is trusted
                    this.authorName.setTooltip(new Tooltip(item.getAuthor().getLongIdName())); // set the tooltip to be the full id
                } else {
                    this.authorName.setTooltip(new Tooltip(hash)); // set the tooltip to be just the hash
                }
            } else {
                this.authorName.setBackground(NOT_VERIFIED_BG); // set the red background
                this.authorName.setTooltip(new Tooltip("NOT VERIFIED: " + hash)); // mark as unverified
            }
        } else {
            this.authorName.setBackground(Background.EMPTY); // clear unverified background
            this.authorName.setText(""); // clear name
            this.authorName.setOnMouseClicked(null); // remove click handler
            // remove all contents
            this.vBox.getChildren().remove(this.imageContents);
            this.vBox.getChildren().remove(this.textMessageContents);
            this.imageContents.setBackground(Background.EMPTY);
            this.textMessageContents.getChildren().clear();

            this.setContextMenu(null); // remove context menu
        }
    }

    /**
     * Copies the author's ID to the clipboard
     */
    private void copyAuthorId() {
        Map<DataFormat, Object> data = new HashMap<>();
        data.put(DataFormat.PLAIN_TEXT, CryptoHelper.sha256Hash(this.getItem().getAuthor().getPublicKey().getEncoded()));
        Clipboard.getSystemClipboard().setContent(data);
    }

    /**
     * Copies the author's name to the clipboard
     */
    private void copyAuthorName() {
        Map<DataFormat, Object> data = new HashMap<>();
        data.put(DataFormat.PLAIN_TEXT, this.getItem().getAuthor().getName());
        Clipboard.getSystemClipboard().setContent(data);
    }

    /**
     * Copies the message contents to the clipboard
     */
    private void copyContents() {
        Map<DataFormat, Object> data = new HashMap<>();
        if (this.getItem().getType() == MessageType.TEXT) {
            data.put(DataFormat.PLAIN_TEXT, ((TextMessage) this.getItem()).getMessage());
        } else {
            data.put(DataFormat.IMAGE, ((ImageMessage) this.getItem()).image());
        }
        Clipboard.getSystemClipboard().setContent(data);
    }

    /**
     * Opens a window describing the author of the message
     */
    private void openAuthor(MouseEvent unused) {
        Message item = this.getItem();
        if (item == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Information");
        alert.setHeaderText(item.getAuthor().getName());
        alert.setContentText("Nickname: " + (this.client.isTrusted(item.getAuthor()) ? this.client.getName(item.getAuthor()) : "N/A") + '\n'
                + "Key ID: " + CryptoHelper.sha256Hash(item.getAuthor().getPublicKey().getEncoded()));
        alert.show();
    }
}
