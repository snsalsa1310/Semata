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

public class Database {
    private static final String SUPABASE_URL = "https://dksolqgiyrkdboxwkmjd.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRrc29scWdpeXJrZGJveHdrbWpkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA0ODgxODYsImV4cCI6MjA2NjA2NDE4Nn0.l9h2Eul46YeaisuzjHzPhig2hiad1cggnfhh_7BOSHo";
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

    public String fetchUserById(String userId) {
        try {
            // Ambil kolom yang dibutuhkan saja. username, nama_profil, dan password_hash
            String uri = SUPABASE_URL + "/rest/v1/users?id=eq." + userId + "&select=username,nama_profil,password_hash";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Response akan berupa array JSON, kita ambil elemen pertama
            if (response.statusCode() == 200 && response.body().startsWith("[")) {
                Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> userList = gson.fromJson(response.body(), type);
                if (!userList.isEmpty()) {
                    return gson.toJson(userList.get(0)); // Kembalikan objek pertama sebagai JSON
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUser(String userId, Map<String, Object> dataToUpdate) {
        try {
            String jsonPayload = gson.toJson(dataToUpdate);
            String uri = SUPABASE_URL + "/rest/v1/users?id=eq." + userId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal") // Tidak perlu response body
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204; // 204 No Content adalah respons sukses untuk PATCH
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public String fetchAllHutang(String userId) {
        try {
            // Mengambil semua utang yang belum lunas
            String uri = SUPABASE_URL + "/rest/v1/utang?id_user=eq." + userId + "&status_lunas=is.false&select=total";
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

    public boolean markUtangAsPaid(String utangId) {
        try {
            // Payload untuk mengupdate status menjadi "Lunas"
            String jsonPayload = "{\"status\": \"Lunas\"}";
            String uri = SUPABASE_URL + "/rest/v1/utang?id=eq." + utangId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            // Untuk PATCH, status 204 (No Content) atau 200 (OK) menandakan sukses
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
