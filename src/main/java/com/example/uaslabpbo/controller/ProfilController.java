package com.example.uaslabpbo.controller;

import com.example.uaslabpbo.config.Database;
import com.example.uaslabpbo.config.UserSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ProfilController {

    @FXML private TextField namaPenggunaField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField ubahPasswordField;
    @FXML private Button simpanButton;
    @FXML private Button batalButton;

    private final Database databaseService = new Database();
    private final Gson gson = new Gson();
    private String originalNamaProfil; // Untuk menyimpan nama asli saat load

    @FXML
    public void initialize() {
        // Nonaktifkan field username karena tidak bisa diubah
        usernameField.setDisable(true);
        loadUserData();
    }

    private void loadUserData() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Sesi tidak ditemukan. Silakan login kembali.");
            return;
        }

        new Thread(() -> {
            String userJson = databaseService.fetchUserById(userId);
            Platform.runLater(() -> {
                if (userJson != null) {
                    Type type = new TypeToken<Map<String, String>>(){}.getType();
                    Map<String, String> userData = gson.fromJson(userJson, type);

                    originalNamaProfil = userData.get("nama_profil");
                    namaPenggunaField.setText(originalNamaProfil);
                    usernameField.setText(userData.get("username"));
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memuat data profil.");
                }
            });
        }).start();
    }

    @FXML
    private void handleSimpanButtonAction(ActionEvent event) {
        String userId = UserSession.getInstance().getUserId();
        String namaBaru = namaPenggunaField.getText().trim();
        String passwordLama = passwordField.getText();
        String passwordBaru = ubahPasswordField.getText();

        // Validasi 1: Password lama harus diisi untuk otentikasi
        if (passwordLama.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Otentikasi Gagal", "Silakan masukkan password Anda saat ini untuk menyimpan perubahan.");
            return;
        }

        // Validasi 2: Cek apakah ada perubahan
        boolean isNamaChanged = !namaBaru.equals(originalNamaProfil);
        boolean isPasswordChanged = !passwordBaru.isEmpty();

        if (!isNamaChanged && !isPasswordChanged) {
            showAlert(Alert.AlertType.INFORMATION, "Tidak Ada Perubahan", "Tidak ada data yang diubah.");
            return;
        }

        simpanButton.setDisable(true);
        simpanButton.setText("Menyimpan...");

        // Proses di background thread
        new Thread(() -> {
            // 1. Ambil data user dari DB untuk verifikasi password lama
            String userJson = databaseService.fetchUserById(userId);
            if (userJson == null) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Gagal mengambil data user untuk verifikasi."));
                return;
            }

            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> userData = gson.fromJson(userJson, type);
            String passwordHashFromDb = userData.get("password_hash");

            // 2. Verifikasi password lama
            if (!BCrypt.checkpw(passwordLama, passwordHashFromDb)) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Otentikasi Gagal", "Password yang Anda masukkan salah.");
                    simpanButton.setDisable(false);
                    simpanButton.setText("SIMPAN");
                });
                return;
            }

            // 3. Jika verifikasi berhasil, siapkan data untuk diupdate
            Map<String, Object> dataToUpdate = new HashMap<>();
            if (isNamaChanged) {
                dataToUpdate.put("nama_profil", namaBaru);
            }
            if (isPasswordChanged) {
                String hashedPasswordBaru = BCrypt.hashpw(passwordBaru, BCrypt.gensalt());
                dataToUpdate.put("password_hash", hashedPasswordBaru);
            }

            // 4. Kirim update ke database
            System.out.println("Data yang akan diupdate: " + dataToUpdate);
            boolean success = databaseService.updateUser(userId, dataToUpdate);
            System.out.println("Status update: " + success);

            // 5. Tampilkan hasil ke user
            Platform.runLater(() -> {
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Profil berhasil diperbarui.");
                    if(isNamaChanged) {
                        UserSession.getInstance().createSession(userId, namaBaru); // Update nama di sesi
                        originalNamaProfil = namaBaru; // Update originalNamaProfil
                        System.out.println("Nama berhasil diubah ke: " + namaBaru);
                    }
                    // Reset form setelah sukses
                    passwordField.clear();
                    ubahPasswordField.clear();

                    // Tunggu sebentar lalu refresh data dari database
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000); // Tunggu 1 detik
                            String updatedUserJson = databaseService.fetchUserById(userId);
                            if (updatedUserJson != null) {
                                Type refreshType = new TypeToken<Map<String, String>>(){}.getType();
                                Map<String, String> refreshedData = gson.fromJson(updatedUserJson, refreshType);
                                Platform.runLater(() -> {
                                    String refreshedNama = refreshedData.get("nama_profil");
                                    System.out.println("Data terbaru dari DB: " + refreshedNama);
                                    if (refreshedNama != null) {
                                        namaPenggunaField.setText(refreshedNama);
                                        originalNamaProfil = refreshedNama;
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui profil di database.");
                }
                simpanButton.setDisable(false);
                simpanButton.setText("SIMPAN");
            });
        }).start();
    }

    @FXML
    private void handleBatalButtonAction(ActionEvent event) {
        // Kembalikan field nama ke nilai semula dan bersihkan field password
        namaPenggunaField.setText(originalNamaProfil);
        passwordField.clear();
        ubahPasswordField.clear();
        showAlert(Alert.AlertType.INFORMATION, "Dibatalkan", "Perubahan telah dibatalkan.");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}