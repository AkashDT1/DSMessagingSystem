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
    
    /** Timeout for checking if a peer node is alive (milliseconds). */
    private static final int PROBE_TIMEOUT_MS = 2000;

    /**
     * Probes a remote server to check if it's currently online and reachable.
     * Use this periodic check to maintain fault-tolerance across the cluster.
     * 
     * @param host Address of the remote node (e.g., 'localhost')
     * @param port Port of the remote node
     * @return true if the node is active, false if it is unreachable
     */
    public boolean isServerReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            // Attempt to establish a TCP connection within the timeout period
            socket.connect(new InetSocketAddress(host, port), PROBE_TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            // Log when a server node becomes unresponsive to notify the cluster
            System.err.println("\n[FAULT] ! Server unreachable: " + host + ":" + port);
            System.err.println("        Reason: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }
}



