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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 * Screen that allows the user to export an (encrypted) account to a file
 */
public class ExportAccountScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Combo box to select what account should be exported
     */
    private final ComboBox<Account> selection;
    /**
     * The current (probably non-primary) stage.
     */
    private final Stage stage;

    public ExportAccountScreen(Client client, Stage stage) {
        this.stage = stage;

        VBox vBox = new VBox();
        JfxUtil.initializePadding(vBox);

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
        Button export = new Button("Export");
        JfxUtil.buttonPressCallback(cancel, stage::close);
        JfxUtil.buttonPressCallback(export, this::export);
        vBox.getChildren().add(JfxUtil.createButtonRow(null, cancel, export));

        Scene scene = new Scene(vBox);
        stage.setTitle("Export account");
        stage.setWidth(350);
        stage.setHeight(120);
        stage.setScene(scene);
    }

    /**
     * Exports the account
     */
    private void export() {
        SingleSelectionModel<Account> selectionModel = this.selection.getSelectionModel();
        if (selectionModel.isEmpty()) return;
        Account selected = selectionModel.getSelectedItem();

        // prompt to select a file to output to
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export account");
        chooser.setInitialFileName(selected.username() + ".account");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Chat account", "*.account"));
        File file = chooser.showSaveDialog(this.stage.getOwner()); //open prompt

        if (file == null) { //no file selected, so cancel
            this.stage.close();
            return;
        }

        // write the selected account out
        try (Writer writer = new FileWriter(file)) {
            Config.GSON.toJson(selected, writer);
            writer.flush();
        } catch (Exception e) {
            LOGGER.fatal("Failed to export account", e);
            return;
        }

        this.stage.close(); // close the window
    }
}
