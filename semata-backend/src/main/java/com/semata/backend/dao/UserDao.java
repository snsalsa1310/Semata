package com.semata.backend.dao;

import com.semata.backend.config.DatabaseConnection;
import com.semata.backend.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDao {
    private final DatabaseConnection dbConnection;

    public UserDao(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, nama_profil, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getNamaProfil());
            pstmt.setString(3, user.getPassword());
            pstmt.executeUpdate();
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setNamaProfil(rs.getString("nama_profil"));
                user.setPassword(rs.getString("password_hash"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
