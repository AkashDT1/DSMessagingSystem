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
     * Executes the election logic using a deterministic priority-based approach.
     * The first reachable server in the prioritized list (serverPriorityList) is elected as leader.
     * 
     * [DEMO NOTE]: This follows the 'Bully' style priority where the lowest port number 
     * (e.g., 5001) is assigned the highest priority. If 5001 is offline, 5002 takes over, and so on.
     * This ensures all nodes agree on a single coordinator without complex voting.
     */
    public synchronized void electLeader() {
        // Iterate through all nodes starting from the highest priority (lowest port)
        // serverPriorityList is expected to be sorted by priority (e.g., 5001, 5002, 5003)
        for (int potentialLeaderPort : serverPriorityList) {
            
            // ROLE 1: If THIS node is the highest priority reachable node, it becomes the LEADER.
            if (potentialLeaderPort == myPort) {
                if (currentLeaderPort != myPort) {
                    currentLeaderPort = myPort;
                    System.out.println("\n[CONSENSUS] COORDINATION UPDATE: No higher priority nodes detected.");
                    System.out.println("[CONSENSUS] ROLE: I am now the ACTIVE LEADER. Responsible for cluster-wide replication.");
                }
                return;
            } 
            
            // ROLE 2: If a higher priority node (lower port) is online, it must be the LEADER.
            if (isServerAlive(potentialLeaderPort)) {
                if (currentLeaderPort != potentialLeaderPort) {
                    currentLeaderPort = potentialLeaderPort;
                    System.out.println("\n[CONSENSUS] COORDINATION UPDATE: Higher priority node detected at port " + potentialLeaderPort);
                    System.out.println("[CONSENSUS] ROLE: I am following " + potentialLeaderPort + " as the coordinator.");
                }
                return;
            }
            
            // If the potential leader is offline, the loop continues to the next highest priority node.
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


