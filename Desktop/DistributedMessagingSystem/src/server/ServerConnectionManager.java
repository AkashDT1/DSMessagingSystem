package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The {@code ServerConnectionManager} handles the incoming TCP connection requests.
 * 
 * <p>It listens on a specified port and acts as an entry point for other 
 * messaging nodes or clients in the distributed system. Once a connection 
 * is accepted, it spawns a dedicated worker thread ({@code ClientHandler}) 
 * to manage subsequent interactions, enabling the server to process concurrent requests.</p>
 */
public class ServerConnectionManager extends Thread {
    private final int port;
    private final MessagingServer messagingServer;

    public ServerConnectionManager(int port, MessagingServer messagingServer) {
        this.port = port;
        this.messagingServer = messagingServer;
    }

    /**
     * Executes the main listening loop for the connection manager.
     * 
     * <p>Initializes the {@code ServerSocket} and iterates continuously to 
     * accept new connections. The loop terminates gracefully if the thread 
     * is interrupted during its runtime.</p>
     */
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[SYSTEM] Messaging Node initialized. Listening for connections on port " + port + ".");

            // The main server loop: waits for and accepts incoming client/node connections.
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                
                // Log connection details to assist in monitoring cluster health and synchronization.
                System.out.println("[RECOV] Connection established with remote peer at " + clientSocket.getRemoteSocketAddress());
                
                // Offload communication logic to a ClientHandler thread
                new ClientHandler(clientSocket, messagingServer).start();
            }
        } catch (IOException e) {
            System.err.println("\n[CRITICAL] Server socket failure on port " + port + ". The port might be busy or restricted.");
            System.err.println("Details: " + e.getMessage() + "\n");
        }
    }
}


