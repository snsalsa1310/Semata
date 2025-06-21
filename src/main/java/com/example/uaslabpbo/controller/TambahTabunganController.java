package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.Database;
import com.example.uaslabpbo.config.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TambahTabunganController {

    @FXML
    private DatePicker tanggalPicker;

    @FXML
    private TextField keteranganField;

    @FXML
    private TextField jumlahField;

    @FXML
    private Button simpanButton;

    @FXML
    private Button batalButton;

    private final Database databaseService = new Database();
    private String tabunganKategoriId; // Akan di-set oleh TabunganController

    public void setTabunganKategoriId(String tabunganKategoriId) {
        this.tabunganKategoriId = tabunganKategoriId;
    }

    @FXML
    private void handleSimpan() {
        // Validasi Input
        if (tanggalPicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Tanggal tidak boleh kosong.");
            return;
        }
        if (keteranganField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Keterangan tidak boleh kosong.");
            return;
        }
        if (jumlahField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Jumlah tidak boleh kosong.");
            return;
        }
        if (tabunganKategoriId == null) {
            showAlert(Alert.AlertType.ERROR, "System Error", "Kategori 'Tabungan' tidak ditemukan untuk user ini.");
            return;
        }

        BigDecimal jumlah;
        try {
            // Menghilangkan "Rp. " dan spasi, lalu parse ke BigDecimal
            String cleanJumlah = jumlahField.getText().replace("Rp.", "").trim();
            jumlah = new BigDecimal(cleanJumlah);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Format jumlah tidak valid. Harap masukkan angka saja.");
            return;
        }

        // Ambil data dari form
        String tanggal = tanggalPicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String keterangan = keteranganField.getText();
        String userId = UserSession.getInstance().getUserId();

        // Buat payload untuk dikirim ke database
        Map<String, Object> transaksiData = Map.of(
                "id_user", userId,
                "id_kategori", tabunganKategoriId,
                "jumlah", jumlah,
                "keterangan", keterangan,
                "tanggal_transaksi", tanggal
        );

        simpanButton.setDisable(true); // Non-aktifkan tombol selama proses
        new Thread(() -> {
            boolean success = databaseService.addTransaksi(transaksiData);
            Platform.runLater(() -> {
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data tabungan berhasil disimpan.");
                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan data tabungan ke database.");
                    simpanButton.setDisable(false);
                }
            });
        }).start();
    }

    @FXML
    private void handleBatal() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) simpanButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}