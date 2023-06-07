package io.github.marcus8448.chat.client.ui;

import io.github.marcus8448.chat.client.Client;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class LoginScreen {
    private static final int CONTENT_HEIGHT = 192 - 25;

    private static final int PADDING = 12;
    private static final int BUTTON_HEIGHT = 25;
    private static final int BUTTON_WIDTH = 70;

    public LoginScreen(Client client, Stage stage) {
        VBox vBox = new VBox();
        MenuItem create = new MenuItem("Create");
        MenuItem importAc = new MenuItem("Import");
        MenuItem export = new MenuItem("Export");
        Menu file = new Menu("Account", null, create, importAc, export);
        MenuItem about = new MenuItem("About");
        Menu help = new Menu("Help", null, about);
        MenuBar menuBar = new MenuBar(file, help);
        vBox.getChildren().add(menuBar);
        VBox.setVgrow(menuBar, Priority.NEVER);
        Insets paddingH = new Insets(0, PADDING, 0, PADDING);
        Insets paddingCore = new Insets(PADDING / 2.0, PADDING, PADDING / 2.0, PADDING);

        Label accountLabel = new Label("Account");
        accountLabel.setPadding(paddingH);
        ComboBox<String> accountBox = new ComboBox<>();
        accountBox.setMaxWidth(1289908123);
        accountBox.setPadding(paddingH);

        HBox accountSelection = new HBox(accountLabel, accountBox);
        accountSelection.setPadding(new Insets(PADDING, PADDING, PADDING / 2.0, PADDING));
        HBox.setHgrow(accountLabel, Priority.NEVER);
        HBox.setHgrow(accountBox, Priority.ALWAYS);
        VBox.setVgrow(accountSelection, Priority.NEVER);
        vBox.getChildren().add(accountSelection);

        Label passwordLabel = new Label("Password");
        passwordLabel.setPadding(paddingH);
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefHeight(25);
        passwordField.setPadding(paddingH);

        HBox passwordInput = new HBox(passwordLabel, passwordField);
        passwordInput.setPadding(paddingCore);
        HBox.setHgrow(passwordLabel, Priority.NEVER);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        VBox.setVgrow(passwordInput, Priority.NEVER);
        vBox.getChildren().add(passwordInput);

        AnchorPane spacing = new AnchorPane();
        VBox.setVgrow(spacing, Priority.ALWAYS);
        vBox.getChildren().add(spacing);

        Label createAccountPrompt = new Label("No account? Create one!");
        createAccountPrompt.setTextFill(Paint.valueOf("#21a7ff"));
        createAccountPrompt.setAlignment(Pos.CENTER);
        AnchorPane spacing2 = new AnchorPane();
        Button cancel = new Button("Cancel");
        cancel.setPrefWidth(BUTTON_WIDTH);
        cancel.setPrefHeight(BUTTON_HEIGHT);
        cancel.setPadding(paddingH);
        Button login = new Button("Login");
        login.setPrefWidth(BUTTON_WIDTH);
        login.setPrefHeight(BUTTON_HEIGHT);
        login.setPadding(paddingH);

        HBox buttons = new HBox(createAccountPrompt, spacing2, cancel, login);
        buttons.setPadding(paddingCore);
        HBox.setHgrow(createAccountPrompt, Priority.NEVER);
        HBox.setHgrow(spacing2, Priority.ALWAYS);
        HBox.setHgrow(cancel, Priority.NEVER);
        HBox.setHgrow(login, Priority.NEVER);
        VBox.setVgrow(buttons, Priority.NEVER);
        vBox.getChildren().add(buttons);

        Scene scene = new Scene(vBox);

        stage.setWidth(400);
        stage.setHeight(200);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
    }
}
