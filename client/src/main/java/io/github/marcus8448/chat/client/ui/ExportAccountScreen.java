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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Base64;


public class ExportAccountScreen {
    private static final int PADDING = 6;
    private static final int WIDTH = 325;
    private static final int HEIGHT = 100;

    private final Client client;
    private final ComboBox<Account> selection;
    private final Stage stage;

    public ExportAccountScreen(Client client, Stage stage) {
        this.client = client;
        this.stage = stage;

        this.selection = new ComboBox<>(client.config.getAccounts());

        this.selection.setPrefWidth(WIDTH - PADDING * 2);
        this.selection.setLayoutX(PADDING);
        this.selection.setLayoutY(PADDING);
        this.selection.setConverter(JfxUtil.ACCOUNT_STRING_CONVERTER);

        Button cancel = new Button("Cancel");
        cancel.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        cancel.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        cancel.setLayoutX(WIDTH - PADDING - JfxUtil.BUTTON_WIDTH - PADDING - JfxUtil.BUTTON_WIDTH);
        cancel.setLayoutY(HEIGHT - 30 - PADDING - JfxUtil.BUTTON_HEIGHT);
        JfxUtil.buttonPressCallback(cancel, stage::close);

        Button export = new Button("Export");
        export.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        export.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        export.setLayoutX(WIDTH - PADDING - JfxUtil.BUTTON_WIDTH);
        export.setLayoutY(HEIGHT - 30 - PADDING - JfxUtil.BUTTON_HEIGHT);
        JfxUtil.buttonPressCallback(export, this::export);

        AnchorPane pane = new AnchorPane(selection, cancel, export);

        Scene scene = new Scene(pane);
        stage.setTitle("Export account");
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setResizable(false);
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
