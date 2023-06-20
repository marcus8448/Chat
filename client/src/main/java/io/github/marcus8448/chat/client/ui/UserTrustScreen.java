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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;


public class UserTrustScreen {
    private static final int PADDING = 12;

    private final ComboBox<User> selection;
    private final TextField username = new TextField();
    private final Stage stage;
    private Client client;

    public UserTrustScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(PADDING));
        vBox.setSpacing(PADDING);

        this.username.setPromptText("username");

        this.selection = new ComboBox<>(client.userList);

        this.selection.setPrefWidth(Integer.MAX_VALUE);
        this.selection.setConverter(new StringConverter<>() {
            @Override
            public String toString(User object) {
                return client.getName(object);
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });
        VBox.setVgrow(this.selection, Priority.NEVER);
        vBox.getChildren().add(this.selection);
        vBox.getChildren().add(this.username);

        Pane padding = new Pane();
        VBox.setVgrow(padding, Priority.ALWAYS);
        vBox.getChildren().add(padding);
        padding.setMinHeight(0);

        Button ok = new Button("Ok");
        ok.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        ok.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        JfxUtil.buttonPressCallback(ok, stage::close);

        Button trust = new Button("Trust");
        this.username.textProperty().addListener((observable, oldValue, newValue) -> trust.setDisable(ParseUtil.validateUsername(newValue).isError()));
        trust.setDisable(true);
        trust.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        trust.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        JfxUtil.buttonPressCallback(trust, this::trust);

        Button revoke = new Button("Revoke");
        revoke.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        revoke.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        JfxUtil.buttonPressCallback(revoke, this::revoke);

        this.selection.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                username.setText(newValue.getSelectedItem().username());
                username.setDisable(false);
                trust.setDisable(false);
                revoke.setDisable(this.client.isTrusted(newValue.getSelectedItem()));
            } else {
                username.setText("");
                username.setDisable(true);
                trust.setDisable(true);
                revoke.setDisable(true);
            }
        });

        Pane pad = new Pane();

        HBox hBox = new HBox(revoke, trust, pad, ok);
        HBox.setHgrow(pad, Priority.ALWAYS);
        HBox.setHgrow(revoke, Priority.NEVER);
        HBox.setHgrow(trust, Priority.NEVER);
        HBox.setHgrow(ok, Priority.NEVER);
        VBox.setVgrow(hBox, Priority.ALWAYS);
        hBox.setSpacing(PADDING);
        vBox.getChildren().add(hBox);

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
        String username = this.username.getText();
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
