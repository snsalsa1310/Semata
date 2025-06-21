package com.semata.backend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.semata.backend.model.User;

import java.util.Date;

public class JwtUtil {
    // Ambil secret key dari environment variable.
    private static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY");
    private static final String FALLBACK_SECRET_KEY = "Semata123!"; // HANYA UNTUK DEVELOPMENT

    private static final Algorithm algorithm = Algorithm.HMAC256((SECRET_KEY != null) ? SECRET_KEY : FALLBACK_SECRET_KEY);

    // Token berlaku selama 24 jam
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    public static String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getId()) // Subject adalah User ID (UUID)
                .withClaim("name", user.getNamaProfil()) // Menambahkan claim tambahan
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public static DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

}
