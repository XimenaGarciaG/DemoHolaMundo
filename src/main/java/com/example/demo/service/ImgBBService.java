package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class ImgBBService {

    @Value("${imgbb.api.key}")
    private String apiKey;

    public String uploadImage(MultipartFile file) throws Exception {
        String apiUrl = "https://api.imgbb.com/1/upload?key=" + apiKey;

        // Convertir archivo a Base64 puro
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        // Codificar en URL Safe para evitar el error "Empty upload source" (el '+' se convierte en ' ')
        String encodedImage = URLEncoder.encode(base64Image, StandardCharsets.UTF_8);
        String requestBody = "image=" + encodedImage;

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error de ImgBB (" + response.statusCode() + "): " + response.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body());
        if (rootNode.has("data") && rootNode.get("data").has("url")) {
            return rootNode.get("data").get("url").asText();
        } else {
            throw new RuntimeException("Respuesta inesperada de ImgBB: " + response.body());
        }
    }
}
