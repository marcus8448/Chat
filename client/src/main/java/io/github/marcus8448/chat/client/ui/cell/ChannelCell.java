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
import io.github.marcus8448.chat.core.api.Constants;
import io.github.marcus8448.chat.core.api.misc.Identifier;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;

public class ChannelCell extends ListCell<Identifier> {
    private final Client client;

    public ChannelCell(Client client) {
        this.client = client;
        this.setEditable(false);
    }

    @Override
    protected void updateItem(Identifier item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            this.setText('#' + item.getValue());
            if (item != Constants.BASE_CHANNEL) {
                MenuItem leave = new MenuItem("Leave");
                leave.setOnAction(e -> this.client.leaveChannel(item));
                this.setContextMenu(new ContextMenu(leave));
            }
        } else {
            this.setText("");
            this.setContextMenu(null);
        }
    }
}
