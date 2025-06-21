package com.semata.backend.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.semata.backend.model.User;
import com.semata.backend.service.UserService;
import com.semata.backend.util.JwtUtil;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;



public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        try {
            User user = ctx.bodyAsClass(User.class);
            userService.registerUser(user);
            ctx.status(201).json(Collections.singletonMap("message", "User registered successfully"));
        } catch (Exception e) {
            ctx.status(400).json(Collections.singletonMap("error", e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            // Dapatkan username dan password dari body request
            Map<String, String> loginCredentials = ctx.bodyAsClass(Map.class);
            String username = loginCredentials.get("username");
            String password = loginCredentials.get("password");

            // Lakukan login dan dapatkan token
            String token = userService.loginUser(username, password);
            ctx.json(Collections.singletonMap("token", token));
        } catch (Exception e) {
            ctx.status(401).json(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * Javalin Access Manager untuk memproteksi endpoint.
     * Ini akan memeriksa JWT pada setiap request ke endpoint yang diproteksi.
     */
    public void accessManager(@NotNull Handler handler, @NotNull Context ctx, @NotNull Set<? extends RouteRole> routeRoles) throws Exception {
        // Untuk saat ini, kita belum mendefinisikan role.
        // Kita hanya akan memproteksi endpoint yang membutuhkan user untuk login.
        // Jika endpoint tidak membutuhkan token, langsung jalankan handler.
        if (routeRoles.isEmpty()) {
            handler.handle(ctx);
            return;
        }

        // Ambil token dari header "Authorization"
        String token = ctx.header("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("No token provided");
        }

        String jwt = token.substring(7); // Hapus "Bearer " dari string token

        try {
            DecodedJWT decodedJWT = JwtUtil.verifyToken(jwt);
            // Simpan informasi user di context agar bisa diakses oleh handler selanjutnya
            ctx.attribute("userId", decodedJWT.getSubject());
            ctx.attribute("userProfileName", decodedJWT.getClaim("name").asString());
            handler.handle(ctx); // Lanjutkan ke handler utama jika token valid
        } catch (Exception e) {
            throw new UnauthorizedResponse("Invalid token");
        }
    }
}
