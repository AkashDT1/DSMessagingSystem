package fault;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * The {@code FailureDetector} is responsible for monitoring the health and accessibility
 * of other server nodes within the distributed messaging system.
 * 
 * <p>It periodically probes remote servers to ensure they are still active and capable
 * of handling requests, which is crucial for maintaining fault tolerance and 
 * high availability in a distributed environment.</p>
 */
public class FailureDetector {
    
    /** Default timeout for socket connection attempts (in milliseconds). */
    private static final int CONNECTION_TIMEOUT_MS = 2000;

    /**
     * Checks whether a remote server node is reachable and accepting connections.
     * 
     * <p>This method attempts to establish a raw TCP connection with the specified
     * host and port. If the connection is successful, the server is considered alive.
     * Otherwise, it is marked as unreachable.</p>
     * 
     * @param host the hostname or IP address of the remote server node
     * @param port the port number on which the remote server is listening
     * @return {@code true} if the server is reachable; {@code false} if the connection 
     *         fails or times out
     */
    public boolean isServerReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            // Attempt to connect to the remote host using a predefined timeout.
            // A timeout is essential to prevent the thread from blocking indefinitely 
            // if the network is sluggish or the remote host is down.
            socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            // If we catch an exception, the node is considered unreachable or faulty.
            System.err.println("[FAULT] Connection attempt failed: " + host + ":" + port + ". Node may be offline or network congested.");
            return false;
        }
    }
}


