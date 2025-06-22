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

public class SidebarController {

    //<editor-fold desc="FXML Fields">
    @FXML private Label namaProfilLabel;
    @FXML private Button anggaranButton;
    @FXML private Button utangButton;
    @FXML private Button tabunganButton;
    @FXML private Button profilButton;
    @FXML private Button keluarButton;
    //</editor-fold>

    // Definisikan style di satu tempat agar mudah diubah
    private final String STYLE_ACTIVE = "-fx-background-color: #3873D9; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10px 20px;";
    private final String STYLE_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #d1eaff; -fx-font-size: 20px; -fx-alignment: CENTER_LEFT; -fx-padding: 10px 20px;";

    @FXML
    public void initialize() {
        String namaProfil = UserSession.getInstance().getNamaProfil();
        if (namaProfil != null && !namaProfil.isEmpty()) {
            namaProfilLabel.setText("Halo, " + namaProfil);
        }
    }

    /**
     * Metode publik yang dipanggil oleh controller halaman utama
     * untuk mengatur tombol mana yang aktif.
     * @param pageName Nama halaman saat ini (misal: "anggaran", "tabungan").
     */
    public void setActiveButton(String pageName) {
        // 1. Reset semua tombol ke style tidak aktif
        anggaranButton.setStyle(STYLE_INACTIVE);
        utangButton.setStyle(STYLE_INACTIVE);
        tabunganButton.setStyle(STYLE_INACTIVE);
        profilButton.setStyle(STYLE_INACTIVE);

        // 2. Atur style aktif untuk tombol yang sesuai
        switch (pageName.toLowerCase()) {
            case "anggaran":
                anggaranButton.setStyle(STYLE_ACTIVE);
                break;
            case "utang":
                utangButton.setStyle(STYLE_ACTIVE);
                break;
            case "tabungan":
                tabunganButton.setStyle(STYLE_ACTIVE);
                break;
            case "profil":
                profilButton.setStyle(STYLE_ACTIVE);
                break;
        }
    }

    //<editor-fold desc="Navigation Methods">
    @FXML private void goToAnggaran(ActionEvent event) { navigateTo(event, "/com/example/uaslabpbo/anggaran.fxml", "Anggaran"); }
    @FXML private void goToHutang(ActionEvent event) { navigateTo(event, "/com/example/uaslabpbo/utang.fxml", "Utang"); }
    @FXML private void goToTabungan(ActionEvent event) { navigateTo(event, "/com/example/uaslabpbo/tabungan.fxml", "Tabungan"); }
    @FXML private void goToProfil(ActionEvent event) { navigateTo(event, "/com/example/uaslabpbo/profil.fxml", "Profil"); }

    @FXML private void handleKeluar(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        navigateTo(event, "/com/example/uaslabpbo/hello-view.fxml", "Semata Login");
    }

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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //</editor-fold>
}