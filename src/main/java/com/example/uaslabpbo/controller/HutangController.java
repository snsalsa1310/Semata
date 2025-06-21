package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.Database;
import com.example.uaslabpbo.config.UserSession;
import com.example.uaslabpbo.model.UtangModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HutangController {

    //<editor-fold desc="FXML Fields">
    @FXML private SidebarController sidebarController; // Injeksi dari FXML
    @FXML private Button tambahUtangButton;
    @FXML private TableView<UtangModel> utangTable;
    @FXML private TableColumn<UtangModel, String> keteranganColumn;
    @FXML private TableColumn<UtangModel, String> jumlahColumn;
    @FXML private TableColumn<UtangModel, String> jatuhTempoColumn;
    @FXML private TableColumn<UtangModel, String> statusLunasColumn;
    @FXML private TableColumn<UtangModel, String> waktuDibuatColumn;
    @FXML private TableColumn<UtangModel, Void> aksiColumn;
    //</editor-fold>

    private final Database databaseService = new Database();
    private final Gson gson = new Gson();
    private ObservableList<UtangModel> utangList = FXCollections.observableArrayList();

    private void setupTableColumns() {
        keteranganColumn.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        jumlahColumn.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        jatuhTempoColumn.setCellValueFactory(new PropertyValueFactory<>("jatuhTempo"));
        statusLunasColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        waktuDibuatColumn.setCellValueFactory(new PropertyValueFactory<>("dibuatPada"));

        aksiColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnLunas = new Button("Tandai Lunas");
            {
                btnLunas.getStyleClass().add("button-lunas"); // Tambahkan style class jika perlu
                btnLunas.setOnAction(event -> {
                    UtangModel utang = getTableView().getItems().get(getIndex());
                    handleTandaiLunas(utang.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UtangModel utang = getTableView().getItems().get(getIndex());
                    if ("Belum Lunas".equals(utang.statusProperty().get())) {
                        setGraphic(btnLunas);
                        setAlignment(Pos.CENTER);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadUtangData() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            System.err.println("UserID is null, cannot load data.");
            return;
        }

        new Thread(() -> {
            String jsonResponse = databaseService.fetchAllHutang(userId);
            if (jsonResponse != null) {
                Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
                List<Map<String, Object>> resultList = gson.fromJson(jsonResponse, type);

                List<UtangModel> tempList = resultList.stream()
                        .map(this::mapToUtangModel)
                        .collect(Collectors.toList());

                utangList = FXCollections.observableArrayList(tempList);

                Platform.runLater(() -> utangTable.setItems(utangList));
            } else {
                Platform.runLater(() -> utangTable.setItems(FXCollections.observableArrayList())); // Kosongkan tabel jika tidak ada data
            }
        }).start();
    }

    // [PERUBAHAN UTAMA DI SINI] Logika parsing dibuat lebih aman
    private UtangModel mapToUtangModel(Map<String, Object> map) {
        try {
            String id = (String) map.get("id");
            String keterangan = (String) map.getOrDefault("nama_utang", "N/A");

            BigDecimal jumlah = BigDecimal.ZERO;
            if (map.get("total") != null) {
                jumlah = new BigDecimal(map.get("total").toString());
            }

            LocalDate jatuhTempo = null;
            if (map.get("jatuh_tempo_pembayaran_berikutnya") != null) {
                jatuhTempo = LocalDate.parse(map.get("jatuh_tempo_pembayaran_berikutnya").toString());
            }

            boolean statusLunas = false;
            Object statusObj = map.get("status_lunas");
            if (statusObj instanceof Boolean) {
                statusLunas = (Boolean) statusObj;
            }

            LocalDate dibuatPada = null;
            Object dibuatObj = map.get("waktu_dibuat");
            if (dibuatObj != null) {
                // Mengambil hanya bagian tanggal dari timestamp
                dibuatPada = LocalDate.parse(dibuatObj.toString().substring(0, 10));
            }

            return new UtangModel(id, keterangan, jumlah, jatuhTempo, statusLunas, dibuatPada);

        } catch (Exception e) {
            System.err.println("Error parsing utang data: " + map.toString());
            e.printStackTrace();
            return null; // Mengembalikan null jika ada error, akan difilter nanti
        }
    }

    @FXML
    private void handleTambahUtang() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uaslabpbo/tambah-utang-popup.fxml"));
            Parent popupRoot = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(tambahUtangButton.getScene().getWindow());
            popupStage.setTitle("Tambah Utang Baru");
            popupStage.setScene(new Scene(popupRoot));
            popupStage.showAndWait();
            loadUtangData(); // Muat ulang data setelah popup ditutup
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTandaiLunas(String utangId) {
        new Thread(() -> {
            boolean success = databaseService.markUtangAsPaid(utangId);
            Platform.runLater(() -> {
                if (success) {
                    loadUtangData(); // Refresh tabel
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Gagal");
                    alert.setContentText("Gagal memperbarui status utang di database.");
                    alert.showAndWait();
                }
            });
        }).start();
    }
}