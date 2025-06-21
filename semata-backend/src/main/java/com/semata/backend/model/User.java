package com.semata.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String id; // UUID
    private String username;

    @JsonProperty("nama_profil") // Agar JSON property cocok dengan nama kolom di DB
    private String namaProfil;

    // Properti password hanya digunakan untuk menerima data saat register/login, tidak untuk dikirim kembali.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNamaProfil() { return namaProfil; }
    public void setNamaProfil(String namaProfil) { this.namaProfil = namaProfil; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
