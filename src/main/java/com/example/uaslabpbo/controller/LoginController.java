package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.Database;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LoginController {
    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button masukButton;

    private final Database userService = new Database();
    private final Gson gson = new Gson();

    @FXML
    protected void handleLogin() {
        String usernameField = username.getText();
        String passwordField = password.getText();

        if (usernameField.isEmpty() || passwordField.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter both username and password.");
            return;
        }
        masukButton.setDisable(true);

        new Thread(() -> {
            String jsonResponse = userService.fetchUserByUsername(usernameField);
            Platform.runLater(() -> {
                processUserResponse(jsonResponse, passwordField);
                masukButton.setDisable(false);
                masukButton.setText("Masuk");
            });
        }).start();
    }

    private void processUserResponse(String jsonResponse, String plainPassword) {
        if (jsonResponse == null) {
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Failed to connect to the server. Please try again later.");
            return;
        }

        Type type = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> userList = gson.fromJson(jsonResponse, type);

        if (userList.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "No user was found with that username.");
            return;
        }

        Map<String, Object> userData = userList.getFirst();
        String storedHash = (String) userData.get("password_hash");

        if (storedHash == null) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "User account is not configured correctly (missing password).");
            return;
        }

        if (BCrypt.checkpw(plainPassword, storedHash)) {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Login Successful");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Welcome back!");
            successAlert.showAndWait();
            navigateToDashboard();

        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "The password you entered is incorrect.");
        }
    }

    private void navigateToDashboard() {
        try {
            Stage stage = (Stage) masukButton.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/uaslabpbo/anggaran.fxml")));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("SEMATA");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the main application screen.");
        }
    }

    @FXML
    protected void back(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uaslabpbo/hello-view.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}