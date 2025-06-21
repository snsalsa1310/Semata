package com.semata.backend.service;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.semata.backend.dao.UserDao;
import com.semata.backend.model.User;
import com.semata.backend.util.JwtUtil;

import java.sql.SQLException;


public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void registerUser(User user) throws SQLException, IllegalArgumentException {
        // Validasi input
        if (user.getUsername() == null || user.getUsername().isEmpty() || user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        // Cek jika username sudah ada
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Hash password sebelum disimpan
        String hashedPassword = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());
        user.setPassword(hashedPassword);

        // Simpan user ke database
        userDao.save(user);
    }

    public String loginUser(String username, String password) throws SQLException, SecurityException {
        // Cari user berdasarkan username
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new SecurityException("Invalid username or password"));

        // Verifikasi password
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        if (!result.verified) {
            throw new SecurityException("Invalid username or password");
        }

        // Jika berhasil, buat JWT token
        return JwtUtil.generateToken(user);
    }
}
