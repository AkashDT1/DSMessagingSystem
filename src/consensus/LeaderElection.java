package consensus;

import fault.FailureDetector;
import java.util.List;

/**
 * Handles deterministic leader election using a priority-based selection algorithm.
 * 
 * CORE LOGIC (Deterministic Consensus):
 * 1. Each server is assigned a static priority based on its port (lower port = higher priority).
 * 2. This ensures all alive nodes always agree on the same leader without expensive voting rounds.
 * 3. Priority Order: 5001 (Highest) > 5002 > 5003 (Lowest).
 * 
 * FAULT TOLERANCE:
 * If the current leader (e.g., 5001) fails, the next available node (5002) detects it and
 * automatically assumes the coordinator role.
 */
public class LeaderElection {
    private final int myPort;
    private final List<Integer> serverPriorityList; // Expected to be sorted by priority
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
     * Executes the leader election process using a prioritized selection (Bully-style).
     * The node with the lowest port number (highest priority) that is currently alive becomes the leader.
     * All other nodes synchronize their state with this leader.
     */
    public synchronized void electLeader() {
        // System.out.println("[CONSENSUS] Running consensus check across cluster nodes...");

        for (int potentialLeaderPort : serverPriorityList) {
            
            // OPTION 1: If I am the highest priority node that hasn't failed, I take the lead.
            if (potentialLeaderPort == myPort) {
                if (currentLeaderPort != myPort) {
                    currentLeaderPort = myPort;
                    System.out.println("\n[CONSENSUS] ROLE TRANSITION: Setting role to ACTIVE_LEADER.");
                    System.out.println("[CONSENSUS] Reason: I am the highest-priority reachable node in the cluster.");
                }
                return;
            } 
            
            // OPTION 2: If a higher-priority node is reachable, I must follow it.
            if (isServerAlive(potentialLeaderPort)) {
                if (currentLeaderPort != potentialLeaderPort) {
                    currentLeaderPort = potentialLeaderPort;
                    System.out.println("\n[CONSENSUS] ROLE TRANSITION: Setting role to FOLLOWER.");
                    System.out.println("[CONSENSUS] Action: Synchronizing with current coordinator at port " + potentialLeaderPort);
                }
                return;
            }
            
            // If the node is unreachable, the election continues checking the next highest priority.
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


