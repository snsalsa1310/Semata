package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AnggaranController {

    // FXML Fields untuk sidebar
    @FXML private Label namaProfilLabel;
    @FXML private Button utangButton;
    @FXML private Button tabunganButton;
    @FXML private Button profilButton;
    @FXML private Button keluarButton;

    // FXML Fields untuk konten (contoh)
    @FXML private Label pengeluaranAmount;
    @FXML private Label pemasukanAmount;
    @FXML private Label sisaSaldo;


    @FXML
    public void initialize() {
        // Metode ini akan dijalankan saat halaman dimuat
        String namaProfil = UserSession.getInstance().getNamaProfil();
        if (namaProfil != null && !namaProfil.isEmpty()) {
            namaProfilLabel.setText("Halo, " + namaProfil);
        }

        // TODO: Tambahkan logika untuk memuat data ringkasan anggaran
        // loadAnggaranSummary();
    }

    // --- Metode Navigasi Sidebar ---

    @FXML
    private void goToHutang(ActionEvent event) {
        navigateTo(event, "/com/example/uaslabpbo/hutang.fxml", "Utang");
    }

    @FXML
    private void goToTabungan(ActionEvent event) {
        navigateTo(event, "/com/example/uaslabpbo/tabungan.fxml", "Tabungan");
    }

    @FXML
    private void goToProfil(ActionEvent event) {
        navigateTo(event, "/com/example/uaslabpbo/profil.fxml", "Profil");
    }

    @FXML
    private void handleKeluar(ActionEvent event) {
        // Bersihkan sesi saat pengguna keluar
        UserSession.getInstance().cleanUserSession();
        navigateTo(event, "/com/example/uaslabpbo/hello-view.fxml", "Semata Login");
    }


    // --- Helper Methods ---

    /**
     * Metode generik untuk berpindah antar halaman FXML.
     */
    private void navigateTo(ActionEvent event, String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Tidak dapat memuat halaman: " + title);
        }
    }

    /**
     * Metode generik untuk menampilkan kotak pesan (alert).
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

