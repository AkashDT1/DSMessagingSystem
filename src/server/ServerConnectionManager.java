package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnectionManager extends Thread {
    private int port;
    private MessagingServer messagingServer;

    public ServerConnectionManager(int port, MessagingServer messagingServer) {
        this.port = port;
        this.messagingServer = messagingServer;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, messagingServer).start();
            }
        } catch (IOException e) {
            System.err.println("\n[ERROR] Could not open port " + port);
            System.err.println("This usually means another program (or an old server) is already using this port.");
            System.err.println("Try typing 'quit' in other windows or restart Eclipse to clear ghost processes.\n");
        }
    }
}
