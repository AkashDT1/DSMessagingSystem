package consensus;

import fault.FailureDetector;
import java.util.List;

/**
 * Handles leader election using a priority-based algorithm.
 * In this system, servers with lower port numbers have higher priority to become the leader.
 * This ensures deterministic consensus across all nodes.
 */
public class LeaderElection {
    private final int myPort;
    private final List<Integer> serverPriorityList; 
    private final FailureDetector healthChecker;
    private int currentLeaderPort;

    public LeaderElection(int myPort, List<Integer> allServerPorts) {
        this.myPort = myPort;
        this.serverPriorityList = allServerPorts;
        this.healthChecker = new FailureDetector();
        this.currentLeaderPort = -1;
        
        System.out.println("[CONSENSUS] Node " + myPort + " initialized consensus module.");
        
        // Initial election on startup
        electLeader();
    }

    /**
     * Executes the election logic using a priority-based approach.
     * The first reachable server in the prioritized list (serverPriorityList) is elected as leader.
     * This follows the 'Bully' style priority where the lowest port (highest priority) 
     * takes responsibility for coordination if it is online.
     */
    public synchronized void electLeader() {
        // System.out.println("[CONSENSUS] Starting election cycle to determine system coordinator...");
        
        // Iterate through all nodes starting from the highest priority (lowest port)
        for (int potentialLeaderPort : serverPriorityList) {
            
            // Case 1: This node has the highest priority among all reachable nodes
            if (potentialLeaderPort == myPort) {
                if (currentLeaderPort != myPort) {
                    currentLeaderPort = myPort;
                    System.out.println("\n[CONSENSUS] COORDINATION UPDATE: No higher priority nodes detected. I am now the Leader/Coordinator!");
                    System.out.println("[CONSENSUS] My role is now to manage cluster-wide message replication.");
                }
                return;
            } 
            
            // Case 2: A higher priority node is reachable
            if (isServerAlive(potentialLeaderPort)) {
                if (currentLeaderPort != potentialLeaderPort) {
                    currentLeaderPort = potentialLeaderPort;
                    System.out.println("\n[CONSENSUS] COORDINATION UPDATE: Higher priority node found at port " + potentialLeaderPort);
                    System.out.println("[CONSENSUS] Following " + potentialLeaderPort + " as the cluster leader for consistency.");
                }
                return;
            }
            
            // If the server is not alive, we continue to the next one in the priority list
        }
    }

    /**
     * @return true if this node is currently the elected leader.
     */
    public boolean amILeader() {
        return myPort == currentLeaderPort;
    }
    
    /**
     * @return The port number of the current leader.
     */
    public int getCurrentLeaderPort() {
        return currentLeaderPort;
    }

    /**
     * Checks if a server at the given port is reachable.
     */
    public boolean isServerAlive(int port) {
        return healthChecker.isServerReachable("localhost", port);
    }
}


