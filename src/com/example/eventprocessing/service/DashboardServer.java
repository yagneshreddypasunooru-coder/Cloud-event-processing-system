package com.example.eventprocessing.service;

import com.example.eventprocessing.model.EventType;
import com.example.eventprocessing.model.ProcessingRecord;
import com.example.eventprocessing.store.EventStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DashboardServer {
    private final int port;
    private final String developerName;
    private final EventIngressApi eventIngressApi;
    private final EventStore eventStore;
    private final AsyncEventPipeline pipeline;
    private final CloudWatchLogger logger;
    private HttpServer server;

    public DashboardServer(int port, String developerName, EventIngressApi eventIngressApi,
                           EventStore eventStore, AsyncEventPipeline pipeline, CloudWatchLogger logger) {
        this.port = port;
        this.developerName = developerName;
        this.eventIngressApi = eventIngressApi;
        this.eventStore = eventStore;
        this.pipeline = pipeline;
        this.logger = logger;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleIndex);
        server.createContext("/styles.css", exchange -> serveStaticFile(exchange, "styles.css", "text/css; charset=utf-8"));
        server.createContext("/app.js", exchange -> serveStaticFile(exchange, "app.js", "application/javascript; charset=utf-8"));
        server.createContext("/api/events", this::handleEvents);
        server.createContext("/api/metrics", this::handleMetrics);
        server.createContext("/api/logs", this::handleLogs);
        server.createContext("/api/health", exchange -> sendJson(exchange, 200, "{\"status\":\"UP\"}"));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    private void handleIndex(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
            return;
        }

        serveStaticFile(exchange, "index.html", "text/html; charset=utf-8");
    }

    private void handleEvents(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            sendJson(exchange, 200, buildEventsJson(eventStore.findAll()));
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            Map<String, String> form = parseForm(readRequestBody(exchange));
            String eventTypeValue = form.getOrDefault("eventType", "");
            String payload = form.getOrDefault("payload", "");

            try {
                EventType eventType = EventType.valueOf(eventTypeValue);
                eventIngressApi.publish(eventType, payload);
                logger.info("Dashboard submitted event type " + eventType + " for " + developerName);
                sendJson(exchange, 201, "{\"message\":\"Event submitted successfully\"}");
            } catch (IllegalArgumentException ex) {
                sendJson(exchange, 400, "{\"error\":\"Invalid event submission\"}");
            }
            return;
        }

        sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
    }

    private void handleMetrics(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
            return;
        }

        Map<String, Integer> metrics = new HashMap<>(logger.snapshotMetrics());
        metrics.put("queue_depth", pipeline.getQueueDepth());

        String json = metrics.entrySet().stream()
                .map(entry -> "\"" + escapeJson(entry.getKey()) + "\":" + entry.getValue())
                .collect(Collectors.joining(",", "{", "}"));
        sendJson(exchange, 200, json);
    }

    private void handleLogs(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
            return;
        }

        List<String> logs = logger.recentLogs();
        String json = logs.stream()
                .map(log -> "\"" + escapeJson(log) + "\"")
                .collect(Collectors.joining(",", "[", "]"));
        sendJson(exchange, 200, json);
    }

    private String buildEventsJson(Collection<ProcessingRecord> records) {
        return records.stream()
                .map(record -> "{"
                        + "\"eventId\":\"" + escapeJson(record.getEventId()) + "\","
                        + "\"eventType\":\"" + record.getEventType() + "\","
                        + "\"status\":\"" + record.getStatus() + "\","
                        + "\"archiveLocation\":\"" + escapeJson(String.valueOf(record.getArchiveLocation())) + "\","
                        + "\"errorMessage\":\"" + escapeJson(String.valueOf(record.getErrorMessage())) + "\","
                        + "\"retryCount\":" + record.getRetryCount() + ","
                        + "\"updatedAt\":\"" + record.getUpdatedAt() + "\""
                        + "}")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private void serveStaticFile(HttpExchange exchange, String fileName, String contentType) throws IOException {
        Path file = Path.of("static", fileName);
        if (!Files.exists(file)) {
            sendText(exchange, 404, "Not Found", "text/plain; charset=utf-8");
            return;
        }

        byte[] body = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(body);
        }
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> values = new HashMap<>();
        if (body.isBlank()) {
            return values;
        }

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = decodeFormComponent(keyValue[0]);
            String value = keyValue.length > 1 ? decodeFormComponent(keyValue[1]) : "";
            values.put(key, value);
        }
        return values;
    }

    private String decodeFormComponent(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        sendText(exchange, statusCode, body, "application/json; charset=utf-8");
    }

    private void sendText(HttpExchange exchange, int statusCode, String body, String contentType) throws IOException {
        byte[] responseBody = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, responseBody.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBody);
        }
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
