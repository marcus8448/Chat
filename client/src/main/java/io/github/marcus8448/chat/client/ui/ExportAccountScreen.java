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
import io.github.marcus8448.chat.client.config.Account;
import io.github.marcus8448.chat.client.config.Config;
import io.github.marcus8448.chat.client.util.JfxUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;


public class ExportAccountScreen {
    private static final int PADDING = 12;

    private final ComboBox<Account> selection;
    private final Stage stage;

    public ExportAccountScreen(Client client, Stage stage) {
        this.stage = stage;

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(PADDING));
        vBox.setSpacing(PADDING);

        this.selection = new ComboBox<>(client.config.getAccounts());

        this.selection.setPrefWidth(Integer.MAX_VALUE);
        this.selection.setConverter(JfxUtil.ACCOUNT_STRING_CONVERTER);
        VBox.setVgrow(this.selection, Priority.NEVER);
        vBox.getChildren().add(this.selection);

        Pane padding = new Pane();
        VBox.setVgrow(padding, Priority.ALWAYS);
        vBox.getChildren().add(padding);
        padding.setMinHeight(0);

        Button cancel = new Button("Cancel");
        cancel.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        cancel.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        JfxUtil.buttonPressCallback(cancel, stage::close);

        Button export = new Button("Export");
        export.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        export.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        JfxUtil.buttonPressCallback(export, this::export);

        Pane pad = new Pane();

        HBox hBox = new HBox(pad, cancel, export);
        HBox.setHgrow(pad, Priority.ALWAYS);
        HBox.setHgrow(cancel, Priority.NEVER);
        HBox.setHgrow(export, Priority.NEVER);
        VBox.setVgrow(hBox, Priority.ALWAYS);
        hBox.setSpacing(PADDING);
        vBox.getChildren().add(hBox);

        Scene scene = new Scene(vBox);
        stage.setTitle("Export account");
        stage.setWidth(350);
        stage.setHeight(125);
        stage.setScene(scene);
    }

    private void export() {
        SingleSelectionModel<Account> selectionModel = this.selection.getSelectionModel();
        if (selectionModel.isEmpty()) return;
        Account selected = selectionModel.getSelectedItem();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export account");
        chooser.setInitialFileName(selected.username() + ".account");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Chat account", "*.account"));
        File file = chooser.showSaveDialog(this.stage.getOwner());
        if (file == null) {
            this.stage.close();
            return;
        }

        try (Writer writer = new FileWriter(file)) {
            Config.GSON.toJson(selected, writer);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.stage.close();
    }
}
