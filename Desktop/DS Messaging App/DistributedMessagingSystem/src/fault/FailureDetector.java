package fault;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Responsibility: Periodically checks if other servers in the 
 * distributed system are still active and reachable.
 */
public class FailureDetector {
    
    private static final int CONNECTION_TIMEOUT_MS = 2000;

    /**
     * Checks if a server is reachable by attempting a socket connection.
     * 
     * @param host The remote server's hostname or IP
     * @param port The remote server's port
     * @return true if connection is successful, false otherwise
     */
    public boolean isServerReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            // Attempt connection with a defined timeout to prevent blocking indefinitely
            socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            // Log could be added here if needed for debugging
            return false;
        }
    }
}

