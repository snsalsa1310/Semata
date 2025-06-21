package com.example.uaslabpbo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button; // Import Button class

import java.io.IOException; // Required for potential scene changes or file operations

public class ProfilController {

    @FXML
    private TextField namaPenggunaField; // Assuming you'll add fx:id="namaPenggunaField" to your Nama Pengguna TextField
    @FXML
    private TextField usernameField; // Assuming you'll add fx:id="usernameField" to your Username TextField
    @FXML
    private PasswordField passwordField; // Assuming you'll add fx:id="passwordField" to your Password PasswordField
    @FXML
    private PasswordField ubahPasswordField; // Assuming you'll add fx:id="ubahPasswordField" to your Ubah Password PasswordField
    @FXML
    private Button simpanButton; // Assuming you'll add fx:id="simpanButton" to your SIMPAN Button
    @FXML
    private Button batalButton; // Assuming you'll add fx:id="batalButton" to your BATAL Button


    // Method called when the controller is initialized
    public void initialize() {
        // You can pre-populate fields with existing user data here if available
        // For now, we'll assume the FXML already has default text like "Aska Skata" etc.
        // namaPenggunaField.setText("Existing Name");
        // usernameField.setText("Existing Username");
    }

    @FXML
    private void handleSimpanButtonAction(ActionEvent event) {
        // 1. Get data from input fields
        String namaPengguna = namaPenggunaField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String ubahPassword = ubahPasswordField.getText();

        // 2. Perform validation (example: check if passwords match)
        if (!password.equals(ubahPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Konfirmasi Password Gagal", "Password dan Ubah Password tidak cocok.");
            return; // Stop further processing
        }

        // 3. (Conceptual) Save the new data
        // In a real application, you would interact with a database,
        // a file, or an API here to persist the data.
        // For demonstration, we'll just print to console.
        System.out.println("Data Profil Baru:");
        System.out.println("Nama Pengguna: " + namaPengguna);
        System.out.println("Username: " + username);
        System.out.println("Password (new/updated): " + password); // Be careful with logging passwords in real apps!

        // 4. Show success pop-up
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data Tersimpan", "Profil Anda berhasil diperbarui.");

        // 5. Optionally, clear fields or navigate to another scene after successful save
        // clearFields(); // You might want to implement this
    }

    @FXML
    private void handleBatalButtonAction(ActionEvent event) {
        // 1. Show confirmation pop-up for cancellation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Batal");
        alert.setHeaderText("Batalkan Perubahan?");
        alert.setContentText("Apakah Anda yakin ingin membatalkan perubahan pada profil Anda?");

        // 2. Handle user's choice
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // User clicked OK (Yes, cancel)
                showAlert(Alert.AlertType.INFORMATION, "Dibatalkan", "Perubahan Dibatalkan", "Perubahan pada profil telah dibatalkan.");
                // Optionally, revert fields to original state or navigate back
                // For now, we'll just clear the password fields and revert others to their initial FXML values.
                passwordField.clear();
                ubahPasswordField.clear();
                // If you fetch initial data in initialize(), you'd re-fetch/reset here.
            } else {
                // User clicked Cancel or closed the dialog
                showAlert(Alert.AlertType.INFORMATION, "Tidak Jadi", "Tidak Dibatalkan", "Anda melanjutkan mengedit profil.");
            }
        });
    }

    // Helper method to show an alert dialog
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Optional: Method to clear all input fields
    private void clearFields() {
        namaPenggunaField.clear();
        usernameField.clear();
        passwordField.clear();
        ubahPasswordField.clear();
    }
}