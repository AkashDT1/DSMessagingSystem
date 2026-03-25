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
            System.out.println(">>> Messaging Server node successfully started on port " + port);

            // Continuously listen for incoming connections unless the thread is closed
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                
                // Offload communication logic to a ClientHandler thread
                new ClientHandler(clientSocket, messagingServer).start();
            }
        } catch (IOException e) {
            System.err.println("\n[ERROR] Server socket failure or port " + port + " is busy.");
            System.err.println("Note: Check for existing processes or verify network permissions.");
            System.err.println("Detailed reason: " + e.getMessage() + "\n");
        }
    }
}


