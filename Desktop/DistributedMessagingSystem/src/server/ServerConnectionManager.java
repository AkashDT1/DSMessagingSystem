package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Responsibility: Listens for incoming client connections (Distributed nodes)
 * and assigns them to worker threads to handle their requests.
 */
public class ServerConnectionManager extends Thread {
    private final int port;
    private final MessagingServer messagingServer;

    public ServerConnectionManager(int port, MessagingServer messagingServer) {
        this.port = port;
        this.messagingServer = messagingServer;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(">>> Messaging Server instance started on port " + port);

            while (!Thread.currentThread().isInterrupted()) {
                // Accepts incoming connections from other messaging nodes or clients
                Socket clientSocket = serverSocket.accept();
                
                // Spawn a new handler for each client for concurrent request processing
                new ClientHandler(clientSocket, messagingServer).start();
            }
        } catch (IOException e) {
            System.err.println("\n[ERROR] Network failure or Port " + port + " is already in use.");
            System.err.println("Suggestion: Check for other running instances or zombie processes.");
            System.err.println("Detailed reason: " + e.getMessage() + "\n");
        }
    }
}

