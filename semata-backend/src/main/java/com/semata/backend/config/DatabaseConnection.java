package com.semata.backend.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Kredensial ini diambil dari Environment Variables untuk keamanan.
    // Pastikan Anda sudah mengatur variabel ini di sistem Anda.
    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    // Fallback ke nilai hardcode JIKA environment variable tidak ditemukan (HANYA UNTUK DEVELOPMENT)
    private static final String FALLBACK_DB_URL = "jdbc:postgresql://db.dksolqgiyrkdboxwkmjd.supabase.co:5432/postgres";
    private static final String FALLBACK_DB_USER = "postgres";
    private static final String FALLBACK_DB_PASSWORD = "Semata123!";

    public Connection getConnection() throws SQLException {
        String url = (DB_URL != null) ? DB_URL : FALLBACK_DB_URL;
        String user = (DB_USER != null) ? DB_USER : FALLBACK_DB_USER;
        String password = (DB_PASSWORD != null) ? DB_PASSWORD : FALLBACK_DB_PASSWORD;

        if (url == null || user == null || password == null) {
            System.err.println("FATAL: Environment variables (DB_URL, DB_USER, DB_PASSWORD) not set!");
            throw new SQLException("Database credentials are not configured.");
        }

        return DriverManager.getConnection(url, user, password);
    }

}
