package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.Database;
import com.example.uaslabpbo.config.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TambahUtangController {

    @FXML private DatePicker tanggalDibuatPicker;
    @FXML private TextField jumlahUtangField;
    @FXML private TextField keteranganUtangField;
    @FXML private DatePicker jatuhTempoPicker;
    @FXML private Button simpanButton;
    @FXML private Button batalButton;

    private final Database databaseService = new Database();

    @FXML
    private void handleSimpanUtang() {
        if (jumlahUtangField.getText().isEmpty() || keteranganUtangField.getText().isEmpty() || jatuhTempoPicker.getValue() == null || tanggalDibuatPicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Semua kolom harus diisi.");
            return;
        }
        // ... validasi lain jika perlu

        Map<String, Object> utangData = Map.of(
                "id_user", UserSession.getInstance().getUserId(),
                "nama_utang", keteranganUtangField.getText(),
                "total", new BigDecimal(jumlahUtangField.getText()),
                "jatuh_tempo_pembayaran_berikutnya", jatuhTempoPicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE),
                "waktu_dibuat", tanggalDibuatPicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE)
        );

        simpanButton.setDisable(true);
        new Thread(() -> {
            boolean success = databaseService.addUtang(utangData);
            Platform.runLater(() -> {
                if(success) {
                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan data ke database.");
                    simpanButton.setDisable(false);
                }
            });
        }).start();
    }

    @FXML private void handleBatal() { closeWindow(); }
    private void closeWindow() { ((Stage) batalButton.getScene().getWindow()).close(); }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}