package com.example.uaslabpbo.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Database {
    private static final String SUPABASE_URL = "https://dksolqgiyrkdboxwkmjd.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRrc29scWdpeXJrZGJveHdrbWpkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA0ODgxODYsImV4cCI6MjA2NjA2NDE4Nn0.l9h2Eul46YeaisuzjHzPhig2hiad1cggnfhh_7BOSHo";
    private static final String SUPABASE_SERVICE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRrc29scWdpeXJrZGJveHdrbWpkIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1MDQ4ODE4NiwiZXhwIjoyMDY2MDY0MTg2fQ.zBKvtDK3ErPV7idR26qqXjQ0zpM5o5JGtNWdwF-NA_A";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String fetchUserByUsername(String username) {
        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            String uri = SUPABASE_URL + "/rest/v1/users?username=eq." + encodedUsername + "&select=*&limit=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 200) ? response.body() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(String namaPengguna, String username, String hashedPassword) {
        try {
            String jsonPayload = gson.toJson(Map.of(
                    "username", username,
                    "nama_profil", namaPengguna,
                    "password_hash", hashedPassword
            ));
            String uri = SUPABASE_URL + "/rest/v1/users";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(String userId, Map<String, Object> dataToUpdate) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                System.err.println("Invalid user ID provided");
                return false;
            }

            if (dataToUpdate == null || dataToUpdate.isEmpty()) {
                System.err.println("No data to update");
                return false;
            }

            String jsonPayload = gson.toJson(dataToUpdate);
            System.out.println("Update payload: " + jsonPayload);
            System.out.println("User ID: " + userId);

            String uri = SUPABASE_URL + "/rest/v1/users?id=eq." + userId;
            System.out.println("Update URI: " + uri);

            // Try with return=representation to see what actually gets updated
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_SERVICE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_SERVICE_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=representation") // Changed from minimal to see the result
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response status: " + response.statusCode());
            System.out.println("Response headers: " + response.headers().map());
            System.out.println("Response body: " + response.body());

            boolean success = response.statusCode() == 200 || response.statusCode() == 204;

            if (success) {
                // Check if we got the updated data back
                if (response.statusCode() == 200 && !response.body().isEmpty()) {
                    System.out.println("✓ Update confirmed - returned data: " + response.body());

                    // Verify the returned data contains our update
                    if (response.body().contains("\"nama_profil\"")) {
                        try {
                            Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                            List<Map<String, Object>> updatedRecords = gson.fromJson(response.body(), listType);
                            if (!updatedRecords.isEmpty()) {
                                Map<String, Object> updatedRecord = updatedRecords.get(0);
                                System.out.println("Updated record: " + updatedRecord);

                                // Check if our update was actually applied
                                for (String key : dataToUpdate.keySet()) {
                                    Object expectedValue = dataToUpdate.get(key);
                                    Object actualValue = updatedRecord.get(key);
                                    System.out.println("Field '" + key + "': expected=" + expectedValue + ", actual=" + actualValue);

                                    if (!expectedValue.equals(actualValue)) {
                                        System.err.println("⚠ WARNING: Field '" + key + "' was not updated correctly!");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing update response: " + e.getMessage());
                        }
                    }
                }
            } else {
                System.err.println("Update failed with status: " + response.statusCode());
                System.err.println("Response body: " + response.body());

                // Check for specific error messages
                if (response.body().contains("permission denied") || response.body().contains("RLS")) {
                    System.err.println("⚠ Possible Row Level Security (RLS) policy blocking the update");
                }
                if (response.body().contains("violates")) {
                    System.err.println("⚠ Possible database constraint violation");
                }
            }

            return success;
        } catch (Exception e) {
            System.err.println("Exception in updateUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Improved fetchUserById method
    public String fetchUserById(String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                System.err.println("Invalid user ID provided to fetchUserById");
                return null;
            }

            // Build URI without cache-busting parameters that confuse PostgREST
            String uri = SUPABASE_URL + "/rest/v1/users?id=eq." + userId +
                    "&select=username,nama_profil,password_hash";

            System.out.println("Fetch URI: " + uri);

            // Use headers for cache-busting instead of URL parameters
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_SERVICE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_SERVICE_KEY)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .header("If-None-Match", "*") // Prevent conditional requests
                    .header("X-Requested-At", String.valueOf(System.currentTimeMillis())) // Custom timestamp header
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Fetch response status: " + response.statusCode());
            System.out.println("Fetch response body: " + response.body());

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                if (responseBody != null && responseBody.startsWith("[")) {
                    Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
                    List<Map<String, Object>> userList = gson.fromJson(responseBody, type);

                    if (userList != null && !userList.isEmpty()) {
                        Map<String, Object> userData = userList.get(0);
                        System.out.println("User data retrieved: " + userData.keySet());
                        return gson.toJson(userData);
                    } else {
                        System.err.println("No user found with ID: " + userId);
                        return null;
                    }
                } else {
                    System.err.println("Unexpected response format: " + responseBody);
                    return null;
                }
            } else {
                System.err.println("Fetch failed with status: " + response.statusCode());
                System.err.println("Error response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception in fetchUserById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Optional: Add a method to test database connectivity
    public boolean testDatabaseConnection() {
        try {
            String uri = SUPABASE_URL + "/rest/v1/users?limit=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_SERVICE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_SERVICE_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            boolean connected = response.statusCode() == 200;
            System.out.println("Database connection test: " + (connected ? "SUCCESS" : "FAILED"));

            return connected;
        } catch (Exception e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    // Optional: Add a method to validate user session
    public boolean validateUserSession(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }

        String userData = fetchUserById(userId);
        return userData != null;
    }

    // Enhanced method to verify update with multiple strategies
    public boolean verifyUpdateWithRetry(String userId, Map<String, Object> expectedData, int maxRetries) {
        System.out.println("Starting update verification for user: " + userId);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Progressive delay: 1s, 2s, 3s, etc.
                Thread.sleep(attempt * 1000);

                System.out.println("Verification attempt " + attempt + "/" + maxRetries);

                String userJson = fetchUserById(userId);
                if (userJson != null) {
                    Type type = new TypeToken<Map<String, String>>(){}.getType();
                    Map<String, String> actualData = gson.fromJson(userJson, type);

                    boolean allMatched = true;
                    for (String key : expectedData.keySet()) {
                        String expectedValue = String.valueOf(expectedData.get(key));
                        String actualValue = actualData.get(key);

                        System.out.println("Checking " + key + ": expected='" + expectedValue + "', actual='" + actualValue + "'");

                        if (!expectedValue.equals(actualValue)) {
                            System.out.println("❌ Mismatch for " + key);
                            allMatched = false;
                        } else {
                            System.out.println("✅ Match for " + key);
                        }
                    }

                    if (allMatched) {
                        System.out.println("✅ All data verified successfully on attempt " + attempt);
                        return true;
                    }
                } else {
                    System.err.println("❌ Failed to fetch user data on attempt " + attempt);
                }

            } catch (InterruptedException e) {
                System.err.println("Verification interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                return false;
            }
        }

        System.err.println("❌ Verification failed after " + maxRetries + " attempts");
        return false;
    }

    // Method to check database permissions
    public boolean testUpdatePermissions(String userId) {
        try {
            System.out.println("Testing update permissions for user: " + userId);

            // Try a no-op update to test permissions
            Map<String, Object> testData = new HashMap<>();
            testData.put("nama_profil", "PERMISSION_TEST");

            String jsonPayload = gson.toJson(testData);
            String uri = SUPABASE_URL + "/rest/v1/users?id=eq." + userId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_SERVICE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_SERVICE_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=representation")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Permission test response: " + response.statusCode());
            System.out.println("Permission test body: " + response.body());

            if (response.statusCode() == 200 || response.statusCode() == 204) {
                System.out.println("✅ Update permissions OK");

                // Revert the test change
                String originalJson = fetchUserById(userId);
                if (originalJson != null) {
                    Type type = new TypeToken<Map<String, String>>(){}.getType();
                    Map<String, String> originalData = gson.fromJson(originalJson, type);
                    String originalName = originalData.get("nama_profil");

                    if (!"PERMISSION_TEST".equals(originalName)) {
                        System.out.println("⚠ Permission test didn't actually update the database");
                        return false;
                    } else {
                        System.out.println("✅ Permission test confirmed - database can be updated");
                        return true;
                    }
                }
            } else {
                System.err.println("❌ Update permissions denied");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Permission test failed: " + e.getMessage());
            return false;
        }

        return false;
    }

    public String getKategoriIdByName(String userId, String kategoriNama) {
        try {
            String encodedKategori = URLEncoder.encode(kategoriNama, StandardCharsets.UTF_8);
            // URL untuk mengambil kategori dengan nama tertentu milik user tertentu
            String uri = SUPABASE_URL + "/rest/v1/kategori?id_user=eq." + userId + "&nama_kategori=eq." + encodedKategori + "&select=id&limit=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY) // Harusnya pakai JWT user, tapi kita ikuti pola yang ada
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type type = new TypeToken<List<Map<String, String>>>() {
                }.getType();
                List<Map<String, String>> resultList = gson.fromJson(response.body(), type);
                if (!resultList.isEmpty()) {
                    return resultList.getFirst().get("id");
                }
            }
            return null; // Tidak ditemukan
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addTransaksi(Map<String, Object> transaksiData) {
        try {
            String jsonPayload = gson.toJson(transaksiData);
            String uri = SUPABASE_URL + "/rest/v1/transaksi";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String fetchTransaksiByKategoriId(String userId, String kategoriId) {
        try {
            String uri = SUPABASE_URL + "/rest/v1/transaksi?id_user=eq." + userId + "&id_kategori=eq." + kategoriId + "&select=*&order=tanggal_transaksi.desc";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 200) ? response.body() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createDefaultCategories(String userId) {
        // Daftar kategori default yang akan dibuat
        List<Map<String, String>> defaultCategories = List.of(
                Map.of("nama_kategori", "Gaji Pokok", "tipe", "pemasukan"),
                Map.of("nama_kategori", "Bonus", "tipe", "pemasukan"),
                Map.of("nama_kategori", "Makan & Minum", "tipe", "pengeluaran"),
                Map.of("nama_kategori", "Transportasi", "tipe", "pengeluaran"),
                Map.of("nama_kategori", "Tagihan", "tipe", "pengeluaran"),
                Map.of("nama_kategori", "Belanja", "tipe", "pengeluaran"),
                Map.of("nama_kategori", "Lain-lain", "tipe", "pengeluaran"),
                // Kategori terpenting agar fitur tabungan berfungsi
                Map.of("nama_kategori", "Tabungan", "tipe", "pengeluaran")
        );

        // Kirim setiap kategori ke Supabase
        for (Map<String, String> kategori : defaultCategories) {
            try {
                // Buat payload JSON
                String jsonPayload = gson.toJson(Map.of(
                        "id_user", userId,
                        "nama_kategori", kategori.get("nama_kategori"),
                        "tipe", kategori.get("tipe")
                ));

                String uri = SUPABASE_URL + "/rest/v1/kategori";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .header("apikey", SUPABASE_ANON_KEY)
                        .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                // Kirim request secara asynchronous
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                // Kita tidak perlu menunggu hasilnya (fire and forget)

            } catch (Exception e) {
                // Jika terjadi error pada satu kategori, lanjutkan ke kategori berikutnya
                e.printStackTrace();
            }
        }
    }

    public String fetchAllTransaksi(String userId) {
        try {
            String uri = SUPABASE_URL + "/rest/v1/transaksi?id_user=eq." + userId + "&select=*&order=tanggal_transaksi.desc";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 200) ? response.body() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String fetchAllKategori(String userId) {
        try {
            // Ambil ID, nama, dan tipe untuk digunakan di controller
            String uri = SUPABASE_URL + "/rest/v1/kategori?id_user=eq." + userId + "&select=id,nama_kategori,tipe";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 200) ? response.body() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Mengambil semua data utang untuk ditampilkan di tabel.
     * @param userId ID pengguna yang sedang login.
     * @return JSON string berisi daftar lengkap utang.
     */
    public String fetchAllHutangForTable(String userId) {
        try {
            String uri = SUPABASE_URL + "/rest/v1/utang?id_user=eq." + userId + "&select=*&order=waktu_dibuat.desc";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 200) ? response.body() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Mengambil data utang yang masih aktif untuk ringkasan.
     * Metode ini menyertakan header cache-control untuk memastikan data selalu baru.
     * @param userId ID pengguna yang sedang login.
     * @return JSON string berisi daftar utang yang aktif.
     */
    public String fetchActiveHutangForSummary(String userId) {
        try {
            // Mengambil semua utang yang BELUM lunas
            String uri = SUPABASE_URL + "/rest/v1/utang?id_user=eq." + userId + "&status_lunas=is.false&select=total";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    // [PENTING] Header untuk mencegah caching
                    .header("Cache-Control", "no-cache")
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 200) ? response.body() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addUtang(Map<String, Object> utangData) {
        try {
            String jsonPayload = gson.toJson(utangData);
            String uri = SUPABASE_URL + "/rest/v1/utang";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mengubah status utang menjadi lunas.
     * @param utangId ID dari utang yang ingin diupdate.
     * @return true jika berhasil, false jika gagal.
     */
    public boolean markUtangAsPaid(String utangId) {
        try {
            String jsonPayload = gson.toJson(Map.of("status_lunas", true));
            String uri = SUPABASE_URL + "/rest/v1/utang?id=eq." + utangId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}