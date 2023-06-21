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
import io.github.marcus8448.chat.core.api.account.User;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;


public class UserTrustScreen {
    private final ComboBox<User> selection;
    private final TextField nickname = new TextField();
    private final Label failureReason = new Label();
    private final Client client;
    private final Stage stage;

    public UserTrustScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;

        VBox vBox = new VBox();
        JfxUtil.initializePadding(vBox);

        this.selection = new ComboBox<>(this.client.userList);

        this.selection.setPrefWidth(Integer.MAX_VALUE);
        this.selection.setConverter(new StringConverter<>() {
            @Override
            public String toString(User object) {
                return object == null ? "" : object.getLongIdName();
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });

        double len = JfxUtil.getTextWidth("Nickname");
        vBox.getChildren().add(JfxUtil.createComboInputRow(new Label("Account"), this.selection, len));
        vBox.getChildren().add(JfxUtil.createInputRow(new Label("Nickname"), this.nickname, "nickname", len));

        JfxUtil.setupFailureLabel(this.failureReason);
        vBox.getChildren().add(this.failureReason);

        Button cancel = new Button("Cancel");
        JfxUtil.buttonPressCallback(cancel, this.stage::close);
        cancel.setPrefWidth(JfxUtil.BUTTON_WIDTH);

        Button trust = new Button("Trust");
        this.nickname.textProperty().addListener((observable, oldValue, newValue) -> trust.setDisable(newValue.isBlank()));
        trust.setDisable(true);
        JfxUtil.buttonPressCallback(trust, this::trust);
        trust.setPrefWidth(JfxUtil.BUTTON_WIDTH);

        Button revoke = new Button("Revoke");
        JfxUtil.buttonPressCallback(revoke, this::revoke);
        revoke.setPrefWidth(JfxUtil.BUTTON_WIDTH);

        JfxUtil.unescapedEnterCallback(this.nickname, this::trust);

        this.selection.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                nickname.setText(newValue.getSelectedItem().username().getValue());
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

        vBox.getChildren().add(JfxUtil.createButtonRow(cancel, null, revoke, trust));

        Scene scene = new Scene(vBox);
        stage.setTitle("Manage Trust");
        JfxUtil.resizeAutoHeight(stage, scene, 700.0);
    }

    private void revoke() {
        SingleSelectionModel<User> selectionModel = this.selection.getSelectionModel();
        if (selectionModel.isEmpty()) {
            this.failureReason.setText("You must select a user");
            return;
        }
        User selected = selectionModel.getSelectedItem();
        this.client.revokeTrust(selected);
        this.stage.close();
    }

    private void trust() {
        SingleSelectionModel<User> selectionModel = this.selection.getSelectionModel();
        if (selectionModel.isEmpty()) {
            this.failureReason.setText("You must select a user");
            return;
        }
        String nickname = this.nickname.getText();
        if (nickname.isBlank()) {
            this.failureReason.setText("Nickname cannot be blank");
            return;
        }
        User selected = selectionModel.getSelectedItem();
        this.client.trustUser(selected, nickname);
        this.stage.close();
    }

    public void select(User item) {
        this.selection.getSelectionModel().select(item);
    }
}
