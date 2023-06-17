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
import io.github.marcus8448.chat.core.user.User;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class UserCell extends ListCell<User> {
    private final Circle picture = new Circle();
    private final Label name = new Label();
    private final Client client;

    public UserCell(Client client) {
        super();
        this.client = client;
        this.setEditable(false);
        HBox.setHgrow(picture, Priority.NEVER);
        VBox.setVgrow(name, Priority.ALWAYS);
        HBox hBox = new HBox(picture, name);
        this.setGraphic(hBox);
        this.setText(null);

        MenuItem trust = new MenuItem("Trust user");
        trust.setOnAction(e -> this.trust());
        ContextMenu contextMenu = new ContextMenu(trust);
        this.setContextMenu(contextMenu);
    }

    private void trust() {

    }

    @Override
    protected void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            this.name.setText(item.usernameAndId());
            this.setOnMouseClicked(this::openAccount);
        }
    }

    private void openAccount(MouseEvent mouseEvent) {
    }
}
