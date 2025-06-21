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
    @FXML private Label pengeluaranAmount;
    @FXML private Label pemasukanAmount;
    @FXML private Label tabunganAmount;
    @FXML private Label hutangAmount;
    @FXML private Button tambahCatatanButton;
    @FXML private VBox pencatatanContainer;
    @FXML private Button sisaSaldo;
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
            String kategoriJson = databaseService.fetchAllKategori(userId);
            kategoriCache = new HashMap<>();
            if (kategoriJson != null) {
                Type type = new TypeToken<List<Map<String, String>>>(){}.getType();
                List<Map<String, String>> kategoriList = gson.fromJson(kategoriJson, type);
                for (Map<String, String> kat : kategoriList) {
                    kategoriCache.put(kat.get("id"), kat);
                }
            }

            String transaksiJson = databaseService.fetchAllTransaksi(userId);

            Platform.runLater(() -> {
                pencatatanContainer.getChildren().clear();
                pencatatanContainer.getChildren().add(createHeaderRow());
                if (transaksiJson != null) {
                    processTransaksiData(transaksiJson);
                } else {
                    pencatatanContainer.getChildren().add(new Label("  Belum ada data pencatatan."));
                }
            });
        }).start();
    }

    private void processTransaksiData(String transaksiJson) {
        Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
        List<Map<String, Object>> transaksiList = gson.fromJson(transaksiJson, type);

        BigDecimal totalPemasukan = BigDecimal.ZERO;
        BigDecimal totalPengeluaran = BigDecimal.ZERO;
        BigDecimal totalTabungan = BigDecimal.ZERO;

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

        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        pemasukanAmount.setText(formatRupiah.format(totalPemasukan));
        pengeluaranAmount.setText(formatRupiah.format(totalPengeluaran));
        tabunganAmount.setText(formatRupiah.format(totalTabungan));

        BigDecimal saldo = totalPemasukan.subtract(totalPengeluaran);
        sisaSaldo.setText(formatRupiah.format(saldo));
    }

    //<editor-fold desc="UI Helper Methods">
    private HBox createHeaderRow() {
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
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        String jenisStyle = "-fx-background-color: #ffe0e0; -fx-background-radius: 5px; -fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-alignment: CENTER;";
        if ("pemasukan".equalsIgnoreCase(tipe)) {
            jenisStyle = "-fx-background-color: #e0ffe0; -fx-background-radius: 5px; -fx-text-fill: #28a745; -fx-font-weight: bold; -fx-alignment: CENTER;";
        }

        HBox row = new HBox(
                createStyledLabel(tanggal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 120, false),
                createStyledLabel(tipe, 100, false, jenisStyle),
                createStyledLabel(namaKategori, 120, false),
                createStyledLabel(keterangan, 200, false),
                createStyledLabel(formatRupiah.format(jumlah), 100, false)
        );
        row.setStyle("-fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;");
        row.setSpacing(20);
        return row;
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