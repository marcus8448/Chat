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
import io.github.marcus8448.chat.client.util.JfxUtil;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class ExportAccountScreen {
    private static final int PADDING = 6;
    private static final int WIDTH = 220;
    private static final int HEIGHT = 100;
    private final Client client;

    public Account selected = null;

    public ExportAccountScreen(Client client, Stage stage) {
        this.client = client;

        ComboBox<Account> selection = new ComboBox<>();
//        selection.setCellFactory(AccountCell::new);
        selection.setPrefWidth(WIDTH - PADDING * 2);
        selection.setLayoutX(PADDING);
        selection.setLayoutY(PADDING);

        Button cancel = new Button("Cancel");
        cancel.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        cancel.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        cancel.setLayoutX(WIDTH - PADDING - JfxUtil.BUTTON_WIDTH - PADDING - JfxUtil.BUTTON_WIDTH);
        cancel.setLayoutY(HEIGHT - 30 - PADDING - JfxUtil.BUTTON_HEIGHT);
        cancel.setOnMouseClicked(e -> stage.close());

        Button export = new Button("Export");
        export.setPrefWidth(JfxUtil.BUTTON_WIDTH);
        export.setPrefHeight(JfxUtil.BUTTON_HEIGHT);
        export.setLayoutX(WIDTH - PADDING - JfxUtil.BUTTON_WIDTH);
        export.setLayoutY(HEIGHT - 30 - PADDING - JfxUtil.BUTTON_HEIGHT);

        AnchorPane pane = new AnchorPane(selection, cancel, export);

        Scene scene = new Scene(pane);

        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setResizable(false);
        stage.setScene(scene);
    }

}
