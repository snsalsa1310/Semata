package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.Database;
import com.example.uaslabpbo.config.UserSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TabunganController {

    @FXML
    private Button nabungButton;
    @FXML
    private VBox tabunganListContainer;
    @FXML
    private Label totalTabunganLabel;

    private final Database databaseService = new Database();
    private final Gson gson = new Gson();
    private String tabunganKategoriId;

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session tidak ditemukan. Silakan login kembali.");
            return;
        }

        new Thread(() -> {
            tabunganKategoriId = databaseService.getKategoriIdByName(userId, "Tabungan");

            if (tabunganKategoriId == null) {
                Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Kategori Hilang", "Kategori 'Tabungan' tidak ditemukan. Harap buat kategori tersebut terlebih dahulu."));
                return;
            }

            String jsonResponse = databaseService.fetchTransaksiByKategoriId(userId, tabunganKategoriId);

            Platform.runLater(() -> {
                tabunganListContainer.getChildren().clear();
                tabunganListContainer.getChildren().add(createHeaderRow());

                if (jsonResponse != null) {
                    Type type = new TypeToken<List<Map<String, Object>>>() {
                    }.getType();
                    List<Map<String, Object>> tabunganList = gson.fromJson(jsonResponse, type);

                    BigDecimal total = BigDecimal.ZERO;
                    for (Map<String, Object> tabunganData : tabunganList) {
                        tabunganListContainer.getChildren().add(createTabunganRow(tabunganData));
                        total = total.add(new BigDecimal(tabunganData.get("jumlah").toString()));
                    }

                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    totalTabunganLabel.setText(formatRupiah.format(total));
                }
            });
        }).start();
    }

    private HBox createHeaderRow() {
        Label tanggalHeader = new Label("Tanggal");
        tanggalHeader.setPrefWidth(120);
        tanggalHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555; -fx-alignment: CENTER;");

        Label kategoriHeader = new Label("Kategori");
        kategoriHeader.setPrefWidth(120);
        kategoriHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555; -fx-alignment: CENTER;");

        Label keteranganHeader = new Label("Keterangan");
        keteranganHeader.setPrefWidth(250);
        keteranganHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555; -fx-alignment: CENTER;");

        Label jumlahHeader = new Label("Jumlah");
        jumlahHeader.setPrefWidth(120);
        jumlahHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555; -fx-alignment: CENTER;");

        HBox header = new HBox(10, tanggalHeader, kategoriHeader, keteranganHeader, jumlahHeader);
        header.setStyle("-fx-padding: 10px; -fx-background-color: #90CAF9;");
        return header;
    }

    private HBox createTabunganRow(Map<String, Object> data) {
        LocalDate tanggal = LocalDate.parse(data.get("tanggal_transaksi").toString());
        String keterangan = (String) data.get("keterangan");
        BigDecimal jumlah = new BigDecimal(data.get("jumlah").toString());

        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        Label tanggalLabel = createStyledLabel(tanggal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 120);
        Label kategoriLabel = createStyledLabel("Tabungan", 120);
        Label keteranganLabel = createStyledLabel(keterangan, 250);
        Label jumlahLabel = createStyledLabel(formatRupiah.format(jumlah), 120);

        HBox row = new HBox(10, tanggalLabel, kategoriLabel, keteranganLabel, jumlahLabel);
        row.setStyle("-fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;");
        return row;
    }

    private Label createStyledLabel(String text, double prefWidth) {
        Label label = new Label(text);
        label.setPrefWidth(prefWidth);
        label.setStyle("-fx-background-color: #D1E5F8; -fx-background-radius: 5px; -fx-padding: 5px; -fx-alignment: CENTER; -fx-text-fill: #333333;");
        return label;
    }

    @FXML
    private void handleNabung() {
        if (tabunganKategoriId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Kategori 'Tabungan' belum siap. Coba lagi sesaat.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uaslabpbo/tambah-tabungan-popup.fxml"));
            Parent popupRoot = loader.load();

            TambahTabunganController controller = loader.getController();
            controller.setTabunganKategoriId(this.tabunganKategoriId);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(nabungButton.getScene().getWindow());
            popupStage.setTitle("Tambah Catatan Tabungan");
            popupStage.setScene(new Scene(popupRoot));
            popupStage.showAndWait();

            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka form tambah tabungan.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}