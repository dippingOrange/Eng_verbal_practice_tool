package com.englishspeaker.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public class AiService {
    private final String endpoint;
    private final String apiKey;
    private final String model;
    private final HttpClient client;
    private final Gson gson = new Gson();

    public AiService() throws IOException {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        }
        this.endpoint = props.getProperty("api.endpoint");
        this.apiKey = props.getProperty("api.key");
        this.model = props.getProperty("api.model", "deepseek-chat");
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String callApi(String systemPrompt, String userMessage) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("model", model);

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);

        body.add("messages", gson.toJsonTree(new JsonObject[]{systemMsg, userMsg}));

        String json = gson.toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return extractContent(response.body());
    }

    private String extractContent(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            return root.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        } catch (Exception e) {
            return "Error: could not parse response - " + e.getMessage();
        }
    }
}
