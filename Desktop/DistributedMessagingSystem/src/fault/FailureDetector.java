package fault;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@code FailureDetector} is the cornerstone of our fault-tolerant design.
 * It monitors the cluster health by periodically checking if peer nodes are active.
 * 
 * <p>In a distributed system, nodes can fail at any time. This component allows the
 * server to detect these failures in real-time and adapt accordingly, ensuring 
 * the messaging service remains available to clients even when parts of the cluster are down.</p>
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
            // Only log once when a server node becomes unresponsive to maintain clean logs
            synchronized (unreachableServers) {
                if (unreachableServers.add(serverId)) {
                    System.err.println("\n[NODE-FAILURE] ! ALERT: Node " + serverId + " is no longer responding.");
                    System.err.println("               Action: Marking as 'UNREACHABLE' and pausing sync protocols.");
                }
            }
            return false;
        }
    }
}



