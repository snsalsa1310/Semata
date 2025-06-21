package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.Database;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // <-- IMPORT BARU
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.lang.reflect.Type; // <-- IMPORT BARU
import java.util.List; // <-- IMPORT BARU
import java.util.Map; // <-- IMPORT BARU
import java.util.Objects;

public class RegisterController {
    @FXML
    private Button registerButton;

    @FXML
    private TextField namaPengguna;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    private final Database userService = new Database();
    private final Gson gson = new Gson(); // <-- INSTANCE BARU

    @FXML
    void handleRegister(ActionEvent event) {
        String namaPenggunaField = namaPengguna.getText();
        String usernameField = username.getText();
        String plainPasswordField = password.getText();

        if (namaPenggunaField.trim().isEmpty() || usernameField.trim().isEmpty() || plainPasswordField.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "All fields are required. Please fill them all out.");
            return;
        }

        if (plainPasswordField.length() < 8) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Password must be at least 8 characters long.");
            return;
        }

        registerButton.setDisable(true);

        // --- [PERUBAHAN LOGIKA DI SINI] ---
        new Thread(() -> {
            String hashedPassword = BCrypt.hashpw(plainPasswordField, BCrypt.gensalt(12));
            boolean success = userService.registerUser(namaPenggunaField, usernameField, hashedPassword);

            if (success) {
                // Jika registrasi berhasil, ambil ID user baru untuk membuat kategori default
                String jsonResponse = userService.fetchUserByUsername(usernameField);
                if (jsonResponse != null) {
                    Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
                    List<Map<String, Object>> userList = gson.fromJson(jsonResponse, type);
                    if (!userList.isEmpty()) {
                        String newUserId = (String) userList.getFirst().get("id");
                        // Panggil metode untuk membuat kategori default
                        userService.createDefaultCategories(newUserId);
                    }
                }
            }

            Platform.runLater(() -> {
                registerButton.setDisable(false);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Your account has been created. Please log in.");
                    navigateToLogin();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", "Could not create account. The username might already be taken.");
                }
            });
        }).start();
        // --- [AKHIR PERUBAHAN LOGIKA] ---
    }

    private void navigateToLogin() {
        try {
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/uaslabpbo/login.fxml")));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void back (ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uaslabpbo/hello-view.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert (Alert.AlertType alertType, String title, String message){
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
