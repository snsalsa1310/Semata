package com.example.uaslabpbo.config;

import com.google.gson.Gson;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Database {
    private static final String SUPABASE_URL = "https://dksolqgiyrkdboxwkmjd.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRrc29scWdpeXJrZGJveHdrbWpkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA0ODgxODYsImV4cCI6MjA2NjA2NDE4Nn0.l9h2Eul46YeaisuzjHzPhig2hiad1cggnfhh_7BOSHo";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String fetchUserByUsername(String email) {
        try {
            String encodedUsername = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String uri = SUPABASE_URL + "/rest/v1/users?username=eq." + encodedUsername + "&select=*&limit=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(String namaPengguna, String username, String hashedPassword) {
        try {
            Map<String, String> userData = Map.of(
                    "username", username,
                    "nama_profil", namaPengguna,
                    "password_hash", hashedPassword
            );

            // Convert the Map to a JSON string
            String jsonPayload = gson.toJson(userData);

            String uri = SUPABASE_URL + "/rest/v1/users";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal") // We don't need the created object back
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // A 201 "Created" status code means success.
            return response.statusCode() == 201;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}