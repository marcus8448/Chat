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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Baggage extends Application {
    private final TextField nameField = new TextField("Pizza Name");
    private final TextField pizzaSize = new TextField("Size (in)");

    private final RadioButton marinara = new RadioButton("marinara");
    private final RadioButton garlic = new RadioButton("garlic");

    private final CheckBox pepperoni = new CheckBox("pepperoni");
    private final CheckBox mushrooms = new CheckBox("mushrooms");
    private final CheckBox pineapple = new CheckBox("pineapple");
    private final CheckBox spinach = new CheckBox("spinach");
    private final CheckBox bacon = new CheckBox("bacon");
    private final Label cost = new Label("The ---------------- will cost $50.00");
    private final Button calculate = new Button("Calculate");

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane pane = new GridPane();
        pane.add(nameField, 0, 0);
        pane.add(pizzaSize, 0, 1);
        pane.add(marinara, 0, 2);
        pane.add(garlic, 0, 3);

        pane.add(pepperoni, 1, 0);
        pane.add(mushrooms, 1, 1);
        pane.add(pineapple, 1, 2);
        pane.add(spinach, 1, 3);
        pane.add(bacon, 1, 4);
        pane.add(cost, 0, 4);
        pane.add(calculate, 1, 5);

        cost.setVisible(false);

        calculate.setOnMouseClicked(a -> {
            double value = 0.0;
            value += Double.parseDouble(pizzaSize.getText());
            value += marinara.selectedProperty().get() ? 2 : 3;
            if (pepperoni.isSelected()) value += 1;
            if (mushrooms.isSelected()) value += 1;
            if (pineapple.isSelected()) value += 1;
            if (spinach.isSelected()) value += 1;
            if (bacon.isSelected()) value += 1;

            cost.setVisible(true);

            cost.setText("The " + nameField.getText() + " will cost $" + value);
        });

        ToggleGroup group = new ToggleGroup();
        marinara.setToggleGroup(group);
        garlic.setToggleGroup(group);

        Scene scene = new Scene(pane, 300, 300);
        primaryStage.setTitle("Pizza cost");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
