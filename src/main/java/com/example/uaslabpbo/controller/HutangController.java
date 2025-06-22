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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HutangController {

    @FXML
    private Button tambahUtangButton;
    @FXML
    private TableView<UtangModel> utangTable;
    @FXML
    private TableColumn<UtangModel, String> keteranganColumn;
    @FXML
    private TableColumn<UtangModel, String> jumlahColumn;
    @FXML
    private TableColumn<UtangModel, String> jatuhTempoColumn;
    @FXML
    private TableColumn<UtangModel, String> statusLunasColumn;
    @FXML
    private TableColumn<UtangModel, String> waktuDibuatColumn;
    @FXML
    private TableColumn<UtangModel, Void> aksiColumn;
    @FXML
    private SidebarController sidebarController;

    private final Database databaseService = new Database();
    private final Gson gson = new Gson();
    private ObservableList<UtangModel> utangList = FXCollections.observableArrayList();

    // [PERBAIKAN 1] Menambahkan metode initialize
    @FXML
    public void initialize() {
        sidebarController.setActiveButton("utang");
        setupTableColumns();
        loadUtangData();
    }

    private void setupTableColumns() {
        keteranganColumn.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        jumlahColumn.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        jatuhTempoColumn.setCellValueFactory(new PropertyValueFactory<>("jatuhTempo"));
        statusLunasColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        waktuDibuatColumn.setCellValueFactory(new PropertyValueFactory<>("dibuatPada"));

        aksiColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnLunas = new Button("Tandai Lunas");

            {
                // Styling opsional untuk tombol
                btnLunas.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnLunas.setOnAction(event -> {
                    UtangModel utang = getTableView().getItems().get(getIndex());
                    handleTandaiLunas(utang.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    // Hanya tampilkan tombol jika status "Belum Lunas"
                    if ("Belum Lunas".equals(getTableRow().getItem().statusProperty().get())) {
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
            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                Type type = new TypeToken<List<Map<String, Object>>>() {
                }.getType();
                List<Map<String, Object>> resultList = gson.fromJson(jsonResponse, type);

                List<UtangModel> tempList = resultList.stream()
                        .map(this::mapToUtangModel)
                        .filter(Objects::nonNull) // Filter objek null jika terjadi error parsing
                        .collect(Collectors.toList());

                utangList.setAll(tempList); // Gunakan setAll untuk update efisien
            } else {
                utangList.clear(); // Kosongkan jika tidak ada data atau response null
            }
            Platform.runLater(() -> utangTable.setItems(utangList));
        }).start();
    }

    private UtangModel mapToUtangModel(Map<String, Object> map) {
        try {
            String id = (String) map.get("id");
            String keterangan = (String) map.getOrDefault("nama_utang", "N/A");

            BigDecimal jumlah = BigDecimal.ZERO;
            if (map.get("total") != null) {
                jumlah = new BigDecimal(map.get("total").toString());
            }

            LocalDate jatuhTempo = null;
            if (map.get("jatuh_tempo_pembayaran_berikutnya") instanceof String) {
                jatuhTempo = LocalDate.parse((String) map.get("jatuh_tempo_pembayaran_berikutnya"));
            }

            LocalDate dibuatPada = null;
            Object dibuatObj = map.get("waktu_dibuat");
            if (dibuatObj instanceof String) {
                dibuatPada = LocalDate.parse(((String) dibuatObj).substring(0, 10));
            }

            boolean statusLunas = false;
            Object statusObj = map.get("status_lunas");
            if (statusObj instanceof Boolean) {
                statusLunas = (Boolean) statusObj;
            }

            return new UtangModel(id, keterangan, jumlah, jatuhTempo, statusLunas, dibuatPada);

        } catch (Exception e) {
            System.err.println("Error parsing utang data: " + map.toString());
            e.printStackTrace();
            return null;
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
            loadUtangData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka form tambah utang.");
        }
    }

    private void handleTandaiLunas(String utangId) {
        new Thread(() -> {
            boolean success = databaseService.markUtangAsPaid(utangId);
            Platform.runLater(() -> {
                if (success) {
                    loadUtangData(); // Refresh tabel
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Status utang berhasil diperbarui.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui status utang di database.");
                }
            });
        }).start();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}