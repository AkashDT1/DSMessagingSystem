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
 * FAULT TOLERANCE & COORDINATION:
 * - If the current leader (e.g., 5001) fails, the next available node (5002) detects it and
 *   automatically assumes the coordinator role.
 * - LEADER IMPACT: The elected leader acts as the sequencer for all incoming messages,
 *   ensuring they are replicated to all followers in the same order.
 */
public class LeaderElection {
    private final int myPort;
    private final List<Integer> serverPriorityList; // Sorted list of potential coordinators
    private final FailureDetector healthChecker;
    private int currentLeaderPort;

    public LeaderElection(int myPort, List<Integer> allServerPorts) {
        this.myPort = myPort;
        this.serverPriorityList = allServerPorts;
        this.healthChecker = new FailureDetector();
        this.currentLeaderPort = -1;
        
        System.out.println("[CONSENSUS] Node " + myPort + " initialized consensus module.");
        
        // Initial election on startup to determine role
        electLeader();
    }

    /**
     * Executes the leader election process using prioritized selection.
     * The node with the highest priority (lowest port number) that is CURRENTLY ALIVE becomes the leader.
     * 
     * WHY THIS WORKS: Since every node runs the same logic over the same prioritized list, 
     * they all converge on the same leader independently (No split-brain).
     */
    public synchronized void electLeader() {
        System.out.println("[DEBUG] Starting election now...");
        int previousLeader = currentLeaderPort;

        for (int potentialLeaderPort : serverPriorityList) {
            System.out.println("[DEBUG] Checking node: " + potentialLeaderPort);
            
            // ROLE DETERMINATION:
            // Check if I am the highest priority node available.
            if (potentialLeaderPort == myPort) {
                currentLeaderPort = myPort;
                if (previousLeader != myPort) {
                    System.out.println("\n[CONSENSUS] COORDINATION UPDATE: Transitioning to ACTIVE_LEADER role.");
                    System.out.println("[CONSENSUS] Responsibility: I am now the single source of truth for message replication.");
                }
                return;
            } 
            
            // Checking if a higher-priority node is reachable.
            if (isServerAlive(potentialLeaderPort)) {
                currentLeaderPort = potentialLeaderPort;
                if (previousLeader != potentialLeaderPort) {
                    System.out.println("\n[CONSENSUS] COORDINATION UPDATE: Transitioning to FOLLOWER role.");
                    System.out.println("[CONSENSUS] Action: Forwarding all client data to coordinator at port " + potentialLeaderPort);
                }
                return;
            }
            
            // Higher priority node is DOWN, proceed to check next potential leader in the list.
        }
    }

    /**
     * Simple method to check if the leader is alive. 
     * If not alive, we must re-elect.
     */
    public void fixLeaderIfDead() {
        if (currentLeaderPort != -1 && !isServerAlive(currentLeaderPort)) {
            System.out.println("[CONSENSUS] Leader is gone! Re-electing now.");
            electLeader();
        }
    }

    /**
     * Helper to verify if this node holds the leader role.
     */
    public boolean amILeader() {
        return myPort == currentLeaderPort;
    }
    
    /**
     * Returns the port of the currently elected leader.
     */
    public int getCurrentLeaderPort() {
        return currentLeaderPort;
    }

    /**
     * Interface with health check module to determine node reachability.
     */
    public boolean isServerAlive(int port) {
        // trying multiple times if it fails
        for (int i = 0; i < 3; i++) {
            if (healthChecker.isServerReachable("localhost", port)) {
                return true;
            }
            System.out.println("[DEBUG] node " + port + " not responding, retrying " + (i + 1));
            try { Thread.sleep(200); } catch (Exception e) {}
        }
        return false;
    }
}


