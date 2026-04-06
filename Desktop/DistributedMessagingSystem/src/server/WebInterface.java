package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

public class WebInterface {
    private MessagingServer server;
    private int httpPort;

    public WebInterface(MessagingServer server, int nodePort) {
        this.server = server;
        // Use a different range for HTTP (e.g., 8001, 8002, etc.)
        this.httpPort = 3000 + (nodePort % 100); 
    }

    public void start() {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
            
            // Endpoint: Get Status
            httpServer.createContext("/status", new StatusHandler());
            
            // Endpoint: Get Messages
            httpServer.createContext("/messages", new MessagesHandler());
            
            // Endpoint: Send Message
            httpServer.createContext("/send", new SendHandler());

            httpServer.setExecutor(null);
            httpServer.start();
            System.out.println("Web Dashboard API started on port " + httpPort);
        } catch (IOException e) {
            System.err.println("Failed to start Web Dashboard API: " + e.getMessage());
        }
    }

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCORSHeaders(exchange);
            String response = String.format(
                "{\"port\": %d, \"isLeader\": %b, \"leaderPort\": %d}",
                server.getMyPort(),
                server.getLeaderElection().amILeader(),
                server.getLeaderElection().getCurrentLeaderPort()
            );
            sendResponse(exchange, response);
        }
    }

    private class MessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCORSHeaders(exchange);
            List<Message> list = server.getMessageStore().getAllMessages();
            String json = "[" + list.stream()
                .map(m -> String.format("{\"sender\":\"%s\",\"content\":\"%s\",\"timestamp\":%d}", 
                    m.getSender(), m.getContent(), m.getTimestamp()))
                .collect(Collectors.joining(",")) + "]";
            sendResponse(exchange, json);
        }
    }

    private class SendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCORSHeaders(exchange);
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                byte[] body = exchange.getRequestBody().readAllBytes();
                String input = new String(body);
                // Expected simplified format: sender|content
                String[] parts = input.split("\\|", 2);
                if (parts.length == 2) {
                    Message msg = new Message(parts[0], "Receiver", parts[1], System.currentTimeMillis());
                    server.processMessage(msg);
                    sendResponse(exchange, "{\"status\":\"delivered\"}");
                } else {
                    sendResponse(exchange, "{\"status\":\"error\",\"message\":\"Invalid format\"}", 400);
                }
            } else {
                sendResponse(exchange, "Method not allowed", 405);
            }
        }
    }

    private void addCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, 200);
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
