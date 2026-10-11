
package com.lodgebilling;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView {

    private final Main main;
    private final VBox root = new VBox(18);

    public LoginView(Main main) {
        this.main = main;
        buildLogin();
    }

    public Parent getView() {
        return root;
    }

    private void prepareRoot() {
        root.getStyleClass().add("login-root");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
    }

    private Label heading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        return label;
    }

    private TextField textField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("input-field");
        return field;
    }

    private PasswordField passwordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.getStyleClass().add("input-field");
        return field;
    }

    private Button button(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private void buildLogin() {
        prepareRoot();
        root.getChildren().clear();

        Label title = heading("LODGE MANAGEMENT");
        Label subtitle = new Label("Billing • Reservations • Guest Services");
        subtitle.getStyleClass().add("subtitle");

        TextField username = textField("Username");
        PasswordField password = passwordField("Password");

        Button loginButton = button("LOGIN");
        Button registerButton = button("CREATE CUSTOMER ACCOUNT");

        Label message = new Label();
        message.getStyleClass().add("message");

        loginButton.setOnAction(e -> {
            if (LodgeData.login(username.getText(), password.getText())) {
                main.showDashboard(username.getText().trim());
            } else {
                message.setText("Incorrect username or password.");
            }
        });

        registerButton.setOnAction(e -> buildRegistration());

        Label demo = new Label("Demo login: admin / admin123");
        demo.getStyleClass().add("hint");

        VBox card = new VBox(15, title, subtitle, username, password,
                loginButton, registerButton, message, demo);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(430);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));

        root.getChildren().add(card);
    }

    private void buildRegistration() {
        prepareRoot();
        root.getChildren().clear();

        Label title = heading("CREATE ACCOUNT");
        Label subtitle = new Label("Choose your own username and password");
        subtitle.getStyleClass().add("subtitle");

        TextField username = textField("Choose a username");
        PasswordField password = passwordField("Choose a password");
        PasswordField confirm = passwordField("Confirm password");

        Button createButton = button("CREATE ACCOUNT");
        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("secondary-button");

        Label message = new Label();
        message.getStyleClass().add("message");

        createButton.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = password.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                message.setText("Enter a username and password.");
            } else if (pass.length() < 6) {
                message.setText("Password must contain at least 6 characters.");
            } else if (!pass.equals(confirm.getText())) {
                message.setText("The passwords do not match.");
            } else if (!LodgeData.register(user, pass)) {
                message.setText("Username already exists. Choose another.");
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Account Created");
                alert.setHeaderText("Registration successful");
                alert.setContentText("You can now log in using your new account.");
                alert.showAndWait();
                buildLogin();
            }
        });

        backButton.setOnAction(e -> buildLogin());

        VBox card = new VBox(15, title, subtitle, username, password,
                confirm, createButton, backButton, message);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(430);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));

        root.getChildren().add(card);
    }
}
