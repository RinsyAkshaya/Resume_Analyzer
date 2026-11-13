package com.resumeanalyser.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Locale;

/** REST client for the Spring Boot backend, built on java.net.http.HttpClient. */
public class ApiClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    // ------------------------------------------------------------------ endpoints

    public JsonNode register(String fullName, String email, String password, String confirmPassword) throws ApiException {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("fullName", fullName);
        body.put("email", email);
        body.put("password", password);
        body.put("confirmPassword", confirmPassword);
        return postJson("/api/auth/register", body);
    }

    public JsonNode login(String email, String password) throws ApiException {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("email", email);
        body.put("password", password);
        return postJson("/api/auth/login", body);
    }

    public JsonNode uploadResume(File file, long userId) throws ApiException {
        return multipart("/api/resumes/upload", "userId", String.valueOf(userId), file);
    }

    public JsonNode analyse(long resumeId, long jobId) throws ApiException {
        HttpRequest req = request("/api/analyse/" + resumeId + "?jobId=" + jobId, Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        return send(req);
    }

    public JsonNode resumes() throws ApiException {
        return get("/api/resumes");
    }

    public JsonNode results(long resumeId) throws ApiException {
        return get("/api/results/" + resumeId);
    }

    public JsonNode recentResults(int limit) throws ApiException {
        return get("/api/results/recent?limit=" + limit);
    }

    public JsonNode jobs() throws ApiException {
        return get("/api/jobs");
    }

    public JsonNode ranking(long jobId) throws ApiException {
        return get("/api/ranking/" + jobId);
    }

    public JsonNode stats() throws ApiException {
        return get("/api/stats");
    }

    // ------------------------------------------------------------------ plumbing

    private HttpRequest.Builder request(String path, Duration timeout) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(timeout).header("Accept", "application/json");
    }

    private JsonNode get(String path) throws ApiException {
        return send(request(path, Duration.ofSeconds(30)).GET().build());
    }

    private JsonNode postJson(String path, JsonNode body) throws ApiException {
        HttpRequest req = request(path, Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        return send(req);
    }

    private JsonNode multipart(String path, String fieldName, String fieldValue, File file) throws ApiException {
        String boundary = "----ResumeAnalyser" + System.nanoTime();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            write(out, "--" + boundary + "\r\n");
            write(out, "Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n");
            write(out, fieldValue + "\r\n");

            String fileName = file.getName().replace("\"", "");
            write(out, "--" + boundary + "\r\n");
            write(out, "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
            write(out, "Content-Type: " + contentType(fileName) + "\r\n\r\n");
            out.write(Files.readAllBytes(file.toPath()));
            write(out, "\r\n--" + boundary + "--\r\n");
        } catch (IOException e) {
            throw new ApiException("Could not read the selected file: " + e.getMessage());
        }
        HttpRequest req = request(path, Duration.ofSeconds(90))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()))
                .build();
        return send(req);
    }

    private static void write(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String contentType(String fileName) {
        String n = fileName.toLowerCase(Locale.ROOT);
        if (n.endsWith(".pdf")) return "application/pdf";
        if (n.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "text/plain";
    }

    private JsonNode send(HttpRequest request) throws ApiException {
        try {
            HttpResponse<String> res = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String body = res.body();
            JsonNode node = (body == null || body.isBlank()) ? MAPPER.nullNode() : MAPPER.readTree(body);
            if (res.statusCode() / 100 == 2) return node;
            String message = node.hasNonNull("message")
                    ? node.get("message").asText()
                    : "Request failed (HTTP " + res.statusCode() + ")";
            throw new ApiException(message, res.statusCode());
        } catch (IOException e) {
            String detail = e.getMessage() == null ? "" : " (" + e.getMessage() + ")";
            throw new ApiException("Cannot reach the backend at " + baseUrl
                    + ". Please start the Spring Boot server first." + detail);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("The request was interrupted.");
        }
    }
}
