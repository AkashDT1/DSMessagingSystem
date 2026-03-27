package fault;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@code FailureDetector} is responsible for monitoring the health and accessibility
 * of other server nodes within the distributed messaging system.
 * 
 * <p>It periodically probes remote servers to ensure they are still active. If a node
 * becomes unreachable, this component flags it so the system can transition to 
 * a fault-tolerant mode (e.g., redirecting requests or pausing replication).</p>
 */
public class FailureDetector {
    
    /** Timeout for checking if a peer node is alive (milliseconds). */
    private static final int PROBE_TIMEOUT_MS = 2000;

    /** Tracks servers currently considered unreachable to prevent log flood and detect recovery. */
    private final Set<String> unreachableServers = new HashSet<>();

    /**
     * Probes a remote server to check if it's currently online and reachable.
     * Use this periodic check to maintain fault-tolerance across the cluster.
     * 
     * @param host Address of the remote node (e.g., 'localhost')
     * @param port Port of the remote node
     * @return true if the node is active, false if it is unreachable
     */
    public boolean isServerReachable(String host, int port) {
        String serverId = host + ":" + port;
        try (Socket socket = new Socket()) {
            // Attempt to establish a TCP connection within the timeout period
            socket.connect(new InetSocketAddress(host, port), PROBE_TIMEOUT_MS);
            
            // If it was previously down, log the recovery event
            synchronized (unreachableServers) {
                if (unreachableServers.remove(serverId)) {
                    System.out.println("[FAULT-RECOVERY] ^ Node " + serverId + " is BACK ONLINE. Restoring sync...");
                }
            }
            return true;
        } catch (IOException e) {
            // Log once when a server node becomes unresponsive to avoid spamming the console
            synchronized (unreachableServers) {
                if (unreachableServers.add(serverId)) {
                    System.err.println("\n[NODE-FAILURE] ! Connection failed for: " + serverId);
                    System.err.println("               Status: Internal failure list updated. Retrying connectivity checks...");
                }
            }
            return false;
        }
    }
}



