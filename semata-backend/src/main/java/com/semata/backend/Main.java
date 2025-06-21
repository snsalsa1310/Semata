package com.semata.backend;

import com.semata.backend.config.DatabaseConnection;
import com.semata.backend.controller.UserController;
import com.semata.backend.dao.UserDao;
import com.semata.backend.service.UserService;
import io.javalin.Javalin;


public class Main {
    public static void main(String[] args) {
        // Inisialisasi komponen-komponen aplikasi
        DatabaseConnection dbConnection = new DatabaseConnection();
        UserDao userDao = new UserDao(dbConnection);
        UserService userService = new UserService(userDao);
        UserController userController = new UserController(userService);

        // Membuat dan mengkonfigurasi server Javalin
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new io.javalin.json.JavalinJackson());
            config.accessManager(userController::accessManager); // Menambahkan Access Manager untuk JWT
        }).start(7070); // Server berjalan di port 7070

        System.out.println("Server Semata Backend berjalan di http://localhost:7070");

        // Mendefinisikan endpoint untuk API
        app.post("/api/register", userController::register);
        app.post("/api/login", userController::login);

        // Contoh endpoint yang dilindungi. Hanya bisa diakses dengan token JWT yang valid.
        app.get("/api/protected-data", ctx -> {
            // Mengambil nama profil dari atribut yang disetel oleh Access Manager
            String userProfileName = ctx.attribute("userProfileName");
            ctx.json("Halo, " + userProfileName + "! Anda berhasil mengakses data terproteksi.");
        });
    }

}
