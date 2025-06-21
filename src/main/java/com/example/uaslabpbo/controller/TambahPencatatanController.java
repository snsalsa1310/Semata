package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.Database;
import com.example.uaslabpbo.config.UserSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TambahPencatatanController {

    //<editor-fold desc="FXML Fields">
    @FXML
    private DatePicker tanggalPicker;
    @FXML
    private MenuButton jenisMenuButton;
    @FXML
    private MenuButton kategoriMenuButton;
    @FXML
    private TextField keteranganField;
    @FXML
    private TextField nominalField;
    @FXML
    private Button simpanButton;
    //</editor-fold>

    //<editor-fold desc="Class Members">
    private final Database databaseService = new Database();
    private final Gson gson = new Gson();

    // Daftar untuk menyimpan kategori dari database
    private List<Map<String, String>> pemasukanKategoriList = new ArrayList<>();
    private List<Map<String, String>> pengeluaranKategoriList = new ArrayList<>();

    // Menyimpan kategori yang dipilih saat ini (termasuk ID-nya)
    private Map<String, String> selectedKategori;
    //</editor-fold>

    @FXML
    public void initialize() {
        tanggalPicker.setValue(LocalDate.now());
        // Hanya memperbolehkan input angka pada field nominal
        nominalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                nominalField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
        loadKategori();
    }

    /**
     * Mengambil data kategori dari database secara asinkron.
     */
    private void loadKategori() {
        new Thread(() -> {
            String userId = UserSession.getInstance().getUserId();
            if (userId == null) return;

            String jsonResponse = databaseService.fetchAllKategori(userId);
            if (jsonResponse != null) {
                Type type = new TypeToken<List<Map<String, String>>>() {
                }.getType();
                List<Map<String, String>> allKategori = gson.fromJson(jsonResponse, type);

                for (Map<String, String> kategori : allKategori) {
                    if ("pemasukan".equalsIgnoreCase(kategori.get("tipe"))) {
                        pemasukanKategoriList.add(kategori);
                    } else if ("pengeluaran".equalsIgnoreCase(kategori.get("tipe"))) {
                        pengeluaranKategoriList.add(kategori);
                    }
                }
            }

            // Update UI di JavaFX Application Thread
            Platform.runLater(() -> handleJenisPemasukan(null));
        }).start();
    }

    /**
     * Memperbarui menu kategori berdasarkan daftar yang diberikan (dari database).
     */
    private void updateKategoriMenu(List<Map<String, String>> kategoriList) {
        kategoriMenuButton.getItems().clear();
        selectedKategori = null; // Reset pilihan

        if (kategoriList.isEmpty()) {
            kategoriMenuButton.setText("Tidak ada kategori");
            kategoriMenuButton.setDisable(true);
            return;
        }

        kategoriMenuButton.setDisable(false);
        for (Map<String, String> kategori : kategoriList) {
            MenuItem menuItem = new MenuItem(kategori.get("nama_kategori"));
            menuItem.setOnAction(event -> {
                selectedKategori = kategori; // Simpan seluruh data kategori yang dipilih
                kategoriMenuButton.setText(kategori.get("nama_kategori"));
            });
            kategoriMenuButton.getItems().add(menuItem);
        }

        // Set pilihan default ke item pertama
        selectedKategori = kategoriList.get(0);
        kategoriMenuButton.setText(selectedKategori.get("nama_kategori"));
    }

    @FXML
    void handleJenisPemasukan(ActionEvent event) {
        jenisMenuButton.setText("Pemasukan");
        jenisMenuButton.setStyle("-fx-background-color: #E0FFE0; -fx-text-fill: #28A745; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 8px 10px; -fx-font-size: 14px;");
        updateKategoriMenu(pemasukanKategoriList);
    }

    @FXML
    void handleJenisPengeluaran(ActionEvent event) {
        jenisMenuButton.setText("Pengeluaran");
        jenisMenuButton.setStyle("-fx-background-color: #FFE0E0; -fx-text-fill: #DC3545; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 8px 10px; -fx-font-size: 14px;");
        updateKategoriMenu(pengeluaranKategoriList);
    }

    @FXML
    void handleSimpan(ActionEvent event) {
        // --- VALIDASI DATA ---
        if (tanggalPicker.getValue() == null || selectedKategori == null ||
                keteranganField.getText().trim().isEmpty() ||
                nominalField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Semua field harus diisi dan kategori harus dipilih!");
            return;
        }

        try {
            // --- KUMPULKAN DATA ---
            Map<String, Object> transaksiData = new HashMap<>();
            transaksiData.put("id_user", UserSession.getInstance().getUserId());
            transaksiData.put("id_kategori", selectedKategori.get("id"));
            transaksiData.put("jumlah", Long.parseLong(nominalField.getText()));
            transaksiData.put("keterangan", keteranganField.getText());
            transaksiData.put("tanggal_transaksi", tanggalPicker.getValue().toString());

            simpanButton.setDisable(true); // Nonaktifkan tombol simpan selama proses
            simpanButton.setText("Menyimpan...");

            // --- PROSES SIMPAN KE DATABASE (BACKGROUND THREAD) ---
            new Thread(() -> {
                boolean success = databaseService.addTransaksi(transaksiData);

                Platform.runLater(() -> {
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Pencatatan berhasil disimpan.");
                        closeWindow();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan pencatatan ke database.");
                        simpanButton.setDisable(false); // Aktifkan kembali tombol jika gagal
                        simpanButton.setText("Simpan");
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Nominal harus berupa angka yang valid.");
        }
    }

    @FXML
    void handleBatal(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nominalField.getScene().getWindow();
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