package com.example.uaslabpbo.config;

/**
 * Kelas ini menggunakan pola Singleton untuk menyimpan informasi pengguna yang sedang login.
 * Ini memastikan hanya ada satu instance sesi di seluruh aplikasi.
 */
public class UserSession {

    private static UserSession instance;

    private String userId;
    private String namaProfil;

    // Constructor dibuat private agar tidak bisa dibuat instance baru dari luar
    private UserSession() {}

    /**
     * Metode untuk mendapatkan satu-satunya instance dari UserSession.
     * @return Instance UserSession.
     */
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Getter dan Setter untuk userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter dan Setter untuk namaProfil
    public String getNamaProfil() {
        return namaProfil;
    }

    public void setNamaProfil(String namaProfil) {
        this.namaProfil = namaProfil;
    }

    /**
     * Membersihkan data sesi, biasanya dipanggil saat logout.
     */
    public void cleanUserSession() {
        userId = null;
        namaProfil = null;
    }
}
