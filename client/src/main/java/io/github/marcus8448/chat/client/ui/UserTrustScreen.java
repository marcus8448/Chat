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
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;


public class UserTrustScreen {
    /**
     * List of users to modify
     */
    private final ComboBox<User> selection;
    /**
     * The nickname input field
     */
    private final TextField nickname = new TextField();
    /**
     * The reason why the nickname could not be set
     */
    private final Label failureReason = new Label();
    /**
     * The client instance
     */
    private final Client client;
    /**
     * The current stagez
     */
    private final Stage stage;

    public UserTrustScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;

        VBox vBox = new VBox();
        JfxUtil.initVbox(vBox);

        this.selection = new ComboBox<>(this.client.userList);

        this.selection.setConverter(new StringConverter<>() { // creates the text in the combo box
            @Override
            public String toString(User object) {
                return object == null ? "" : object.getLongIdName();
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });

        //input rows
        double len = JfxUtil.getTextWidth("Nickname");
        vBox.getChildren().add(JfxUtil.createComboInputRow(new Label("Account"), this.selection, len));
        vBox.getChildren().add(JfxUtil.createInputRow(new Label("Nickname"), this.nickname, "nickname", len));

        // failure label
        JfxUtil.setupFailureLabel(this.failureReason);
        vBox.getChildren().add(this.failureReason);


        // buttons
        Button cancel = new Button("Cancel");
        JfxUtil.buttonPressCallback(cancel, this.stage::close);
        cancel.setPrefWidth(JfxUtil.BUTTON_WIDTH);

        Button trust = new Button("Trust");
        JfxUtil.buttonPressCallback(trust, this::trust);
        trust.setPrefWidth(JfxUtil.BUTTON_WIDTH);

        Button revoke = new Button("Revoke");
        JfxUtil.buttonPressCallback(revoke, this::revoke);
        revoke.setPrefWidth(JfxUtil.BUTTON_WIDTH);

        JfxUtil.unescapedEnterCallback(this.nickname, this::trust);

        this.selection.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nickname.setText(newValue.getName()); // when the selection changes, set the nickname to the username
            }
        });
        // disable buttons when in invalid state
        this.nickname.disableProperty().bind(this.selection.getSelectionModel().selectedItemProperty().isNull());
        revoke.disableProperty().bind(this.selection.getSelectionModel().selectedItemProperty().map(m -> m == null || !this.client.isTrusted(m)));
        trust.disableProperty()
                .bind(BooleanBinding.booleanExpression(this.selection.getSelectionModel().selectedItemProperty().isNull())
                        .or(BooleanBinding.booleanExpression(this.nickname.textProperty().map(String::isBlank))));

        vBox.getChildren().add(JfxUtil.createButtonRow(cancel, null, revoke, trust));

        Scene scene = new Scene(vBox);
        stage.setTitle("Manage Trust");
        JfxUtil.resizeAutoHeight(stage, scene, 700.0);
    }

    /**
     * Revoke the selected user's trust
     */
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

    /**
     * Trust the selected user
     */
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

    /**
     * selects the given item
     *
     * @param item the item to select
     */
    public void select(User item) {
        this.selection.getSelectionModel().select(item);
    }
}
