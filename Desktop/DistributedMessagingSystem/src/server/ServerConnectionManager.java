package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The {@code ServerConnectionManager} handles incoming TCP connection requests.
 * 
 * <p>It acts as the primary listener for the messaging server, allowing peer
 * servers or clients to establish communication channels. This is essential 
 * for nodes recovering from failures to quickly reconnect to the cluster.</p>
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
                
                // Track node-to-node network activity for demo/monitoring
                System.out.println("[NODE-ACTIVITY] Cluster node at " + clientSocket.getRemoteSocketAddress() + " joined the server for message replication.");
                
                // Spawning worker to process current context and help keep node stay responsive 
                new ClientHandler(clientSocket, messagingServer).start();
            }
        } catch (IOException e) {
            System.err.println("\n[FATAL-NETWORK] Port " + port + " could not be opened.");
            System.err.println("                Reason: Already in use or insufficient privileges. Shutting down node.");
        }
    }

}


