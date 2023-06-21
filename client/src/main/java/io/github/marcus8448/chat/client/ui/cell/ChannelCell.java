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
