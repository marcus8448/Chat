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
import io.github.marcus8448.chat.client.util.JfxUtil;
import io.github.marcus8448.chat.client.util.ParseUtil;
import io.github.marcus8448.chat.core.api.account.User;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;


public class UserTrustScreen {
    private final ComboBox<User> selection;
    private final TextField nickname = new TextField();
    private final Client client;

    public UserTrustScreen(Client client, Stage stage) {
        this.client = client;

        VBox vBox = new VBox();
        JfxUtil.initializePadding(vBox);

        this.selection = new ComboBox<>(this.client.userList);

        this.selection.setPrefWidth(Integer.MAX_VALUE);
        this.selection.setConverter(new StringConverter<>() {
            @Override
            public String toString(User object) {
                return object.getFormattedName();
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });
        VBox.setVgrow(this.selection, Priority.NEVER);
        vBox.getChildren().add(this.selection);
        vBox.getChildren().add(JfxUtil.createInputRow(new Label("Nickname"), this.nickname, "nickname", -1));

        Button ok = new Button("Ok");
        JfxUtil.buttonPressCallback(ok, stage::close);

        Button trust = new Button("Trust");
        this.nickname.textProperty().addListener((observable, oldValue, newValue) -> trust.setDisable(ParseUtil.validateUsername(newValue).isError()));
        trust.setDisable(true);
        JfxUtil.buttonPressCallback(trust, this::trust);

        Button revoke = new Button("Revoke");
        JfxUtil.buttonPressCallback(revoke, this::revoke);

        this.selection.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                nickname.setText(newValue.getSelectedItem().username());
                nickname.setDisable(false);
                trust.setDisable(false);
                revoke.setDisable(this.client.isTrusted(newValue.getSelectedItem()));
            } else {
                nickname.setText("");
                nickname.setDisable(true);
                trust.setDisable(true);
                revoke.setDisable(true);
            }
        });

        vBox.getChildren().add(JfxUtil.createButtonRow(revoke, trust, null, ok));

        Scene scene = new Scene(vBox);
        stage.setTitle("Export account");
        stage.setWidth(350);
        stage.setHeight(170);
        stage.setScene(scene);
    }

    private void revoke() {
        SingleSelectionModel<User> selectionModel = this.selection.getSelectionModel();
        if (selectionModel.isEmpty()) return;
        User selected = selectionModel.getSelectedItem();
        this.client.revokeTrust(selected);
    }

    private void trust() {
        SingleSelectionModel<User> selectionModel = this.selection.getSelectionModel();
        String username = this.nickname.getText();
        if (ParseUtil.validateUsername(username).isError()) {
            return;
        }
        if (selectionModel.isEmpty()) return;
        User selected = selectionModel.getSelectedItem();
        this.client.trustUser(selected, username);
    }

    public void select(User item) {
        this.selection.getSelectionModel().select(item);
    }
}
