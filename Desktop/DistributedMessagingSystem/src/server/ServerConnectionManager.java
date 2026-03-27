package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The {@code ServerConnectionManager} is the entry point for all network communication.
 * 
 * <p>This background thread continuously listens for incoming connections from both
 * clients and peer servers. Its robust design ensures that self-healing nodes 
 * can seamlessly reconnect and reintegrate into the cluster after a failure.</p>
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
            System.out.println("[SYSTEM-UP] Socket initialized on Port " + port + ". Listening for cluster traffic...");

            // Main loop: accepting incoming requests for data/message sync
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                
                // Logging incoming connections helps demonstrate real-time cluster dynamics during valls
                System.out.println("[RECONNECTION-FLOW] Initializing handshake with: " + clientSocket.getRemoteSocketAddress());
                
                // Spawn a handler to keep the main connection loop non-blocking and responsive
                new ClientHandler(clientSocket, messagingServer).start();
            }
        } catch (IOException e) {
            System.err.println("\n[FATAL-NETWORK] Port " + port + " could not be opened.");
            System.err.println("                Reason: Already in use or insufficient privileges. Shutting down node.");
        }
    }

}


