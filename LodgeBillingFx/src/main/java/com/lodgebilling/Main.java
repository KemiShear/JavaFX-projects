
package com.lodgebilling;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Lodge Billing Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        showLogin();
        stage.show();
    }

    public void showLogin() {
        LoginView login = new LoginView(this);
        Scene scene = new Scene(login.getView(), 1000, 650);
        applyStylesheet(scene);
        stage.setScene(scene);
        stage.setTitle("Lodge Billing | Login");
        stage.centerOnScreen();
    }

    public void showDashboard(String username) {
        DashboardView dashboard = new DashboardView(this, username);
        Scene scene = new Scene(dashboard.getView(), 1200, 750);
        applyStylesheet(scene);
        stage.setScene(scene);
        stage.setTitle("Lodge Billing | Dashboard");
        stage.centerOnScreen();
    }

    private void applyStylesheet(Scene scene) {
        var css = getClass().getResource("/com/lodgebilling/style.css");

        if (css == null) {
            new Alert(Alert.AlertType.ERROR,
                    "style.css was not found in src/main/resources/com/lodgebilling/")
                    .showAndWait();
            return;
        }

        scene.getStylesheets().add(css.toExternalForm());
    }

    public Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
