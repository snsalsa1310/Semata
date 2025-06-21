package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.Database;
import com.example.uaslabpbo.config.UserSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AnggaranController {

    //<editor-fold desc="FXML Fields">
    @FXML private Label namaProfilLabel;
    @FXML private Button utangButton;
    @FXML private Button tabunganButton;
    @FXML private Button profilButton;
    @FXML private Button keluarButton;
    @FXML private Button tambahCatatanButton;
    @FXML private VBox pencatatanContainer;
    @FXML private Button sisaSaldo;
    @FXML private VBox ringkasanContainer;
    //</editor-fold>

    private final Database databaseService = new Database();
    private final Gson gson = new Gson();
    private Map<String, Map<String, String>> kategoriCache;

    @FXML
    public void initialize() {
        String namaProfil = UserSession.getInstance().getNamaProfil();
        if (namaProfil != null && !namaProfil.isEmpty()) {
            namaProfilLabel.setText("Halo, " + namaProfil);
        }
        loadData();
    }

    private void loadData() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Sesi pengguna tidak ditemukan.");
            return;
        }

        new Thread(() -> {
            // Ambil semua data yang dibutuhkan secara bersamaan
            String kategoriJson = databaseService.fetchAllKategori(userId);
            String transaksiJson = databaseService.fetchAllTransaksi(userId);
            String hutangJson = databaseService.fetchAllHutang(userId); // <-- Ambil data hutang

            // Cache kategori
            kategoriCache = new HashMap<>();
            if (kategoriJson != null) {
                Type type = new TypeToken<List<Map<String, String>>>(){}.getType();
                List<Map<String, String>> kategoriList = gson.fromJson(kategoriJson, type);
                for (Map<String, String> kat : kategoriList) {
                    kategoriCache.put(kat.get("id"), kat);
                }
            }

            // Update UI setelah semua data siap
            Platform.runLater(() -> {
                processAllData(transaksiJson, hutangJson);
            });
        }).start();
    }

    private void processAllData(String transaksiJson, String hutangJson) {
        // Proses Transaksi
        BigDecimal totalPemasukan = BigDecimal.ZERO;
        BigDecimal totalPengeluaran = BigDecimal.ZERO;
        BigDecimal totalTabungan = BigDecimal.ZERO;

        pencatatanContainer.getChildren().clear();
        pencatatanContainer.getChildren().add(createHeaderRowPencatatan());

        if (transaksiJson != null) {
            Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String, Object>> transaksiList = gson.fromJson(transaksiJson, type);
            for (Map<String, Object> trx : transaksiList) {
                String kategoriId = (String) trx.get("id_kategori");
                Map<String, String> kategoriData = kategoriCache.get(kategoriId);
                if (kategoriData == null) continue;

                String namaKategori = kategoriData.get("nama_kategori");
                String tipe = kategoriData.get("tipe");
                BigDecimal jumlah = new BigDecimal(trx.get("jumlah").toString());

                if ("pemasukan".equalsIgnoreCase(tipe)) {
                    totalPemasukan = totalPemasukan.add(jumlah);
                } else {
                    totalPengeluaran = totalPengeluaran.add(jumlah);
                    if ("Tabungan".equalsIgnoreCase(namaKategori)) {
                        totalTabungan = totalTabungan.add(jumlah);
                    }
                }
                pencatatanContainer.getChildren().add(createTransaksiRow(trx, tipe, namaKategori));
            }
        }

        // Proses Hutang
        BigDecimal totalHutang = BigDecimal.ZERO;
        if (hutangJson != null) {
            Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String, Object>> hutangList = gson.fromJson(hutangJson, type);
            for (Map<String, Object> hutang : hutangList) {
                totalHutang = totalHutang.add(new BigDecimal(hutang.get("total").toString()));
            }
        }

        // [PERUBAHAN UTAMA] Update Ringkasan
        updateRingkasanUI(totalPemasukan, totalPengeluaran, totalTabungan, totalHutang);

        // Update Sisa Saldo
        BigDecimal saldo = totalPemasukan.subtract(totalPengeluaran);
        sisaSaldo.setText(formatRupiah(saldo));
    }

    private void updateRingkasanUI(BigDecimal pemasukan, BigDecimal pengeluaran, BigDecimal tabungan, BigDecimal hutang) {
        // Hapus baris lama, sisakan header
        while(ringkasanContainer.getChildren().size() > 2) {
            ringkasanContainer.getChildren().removeLast();
        }

        // Tambahkan baris-baris baru yang sudah dihitung
        ringkasanContainer.getChildren().add(createRingkasanRow("Pemasukan", pemasukan));
        ringkasanContainer.getChildren().add(createRingkasanRow("Pengeluaran", pengeluaran));
        ringkasanContainer.getChildren().add(createRingkasanRow("Tabungan", tabungan));
        ringkasanContainer.getChildren().add(createRingkasanRow("Hutang", hutang));
    }

    //<editor-fold desc="UI Helper Methods">

    private HBox createRingkasanRow(String kategori, BigDecimal jumlah) {
        Label kategoriLabel = new Label(kategori);
        kategoriLabel.setPrefWidth(300);
        kategoriLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        kategoriLabel.setPadding(new Insets(0, 0, 0, 10));

        Label jumlahLabel = new Label(formatRupiah(jumlah));
        jumlahLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555; -fx-alignment: CENTER_RIGHT;");

        HBox row = new HBox(kategoriLabel, jumlahLabel);
        HBox.setHgrow(jumlahLabel, Priority.ALWAYS); // Agar jumlah rata kanan
        row.setStyle("-fx-padding: 5px 0; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;");
        return row;
    }

    private HBox createHeaderRowPencatatan() {
        HBox header = new HBox(
                createStyledLabel("Tanggal", 120, true),
                createStyledLabel("Jenis", 100, true),
                createStyledLabel("Kategori", 120, true),
                createStyledLabel("Keterangan", 200, true),
                createStyledLabel("Jumlah", 100, true)
        );
        header.setStyle("-fx-background-color: #90CAF9; -fx-padding: 10px;");
        header.setSpacing(20);
        return header;
    }

    private HBox createTransaksiRow(Map<String, Object> trx, String tipe, String namaKategori) {
        String tanggalStr = trx.get("tanggal_transaksi").toString();
        LocalDate tanggal = LocalDate.parse(tanggalStr);
        String keterangan = (String) trx.get("keterangan");
        BigDecimal jumlah = new BigDecimal(trx.get("jumlah").toString());

        HBox row = new HBox(
                createStyledLabel(tanggal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 120, false),
                createStyledLabel(tipe, 100, false, getJenisStyle(tipe)),
                createStyledLabel(namaKategori, 120, false),
                createStyledLabel(keterangan, 200, false),
                createStyledLabel(formatRupiah(jumlah), 100, false)
        );
        row.setStyle("-fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;");
        row.setSpacing(20);
        return row;
    }

    private String getJenisStyle(String tipe) {
        if ("pemasukan".equalsIgnoreCase(tipe)) {
            return "-fx-background-color: #e0ffe0; -fx-background-radius: 5px; -fx-text-fill: #28a745; -fx-font-weight: bold; -fx-alignment: CENTER;";
        }
        return "-fx-background-color: #ffe0e0; -fx-background-radius: 5px; -fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-alignment: CENTER;";
    }


    private Label createStyledLabel(String text, double prefWidth, boolean isHeader) {
        return createStyledLabel(text, prefWidth, isHeader, "");
    }

    private Label createStyledLabel(String text, double prefWidth, boolean isHeader, String customStyle) {
        Label label = new Label(text);
        label.setPrefWidth(prefWidth);
        if (isHeader) {
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        } else if (!customStyle.isEmpty()) {
            label.setStyle(customStyle);
        } else {
            label.setStyle("-fx-text-fill: #333333;");
        }
        label.setPadding(new Insets(5));
        return label;
    }

    private String formatRupiah(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(amount);
    }
    //</editor-fold>

    //<editor-fold desc="Navigation and Alerts">
    @FXML private void goToHutang(ActionEvent event) { navigateTo(event, "/com/example/uaslabpbo/hutang.fxml", "Utang"); }
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