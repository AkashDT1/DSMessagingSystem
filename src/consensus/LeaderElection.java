package consensus;

import fault.FailureDetector;
import java.util.List;

/**
 * Handles leader election using a priority-based algorithm.
 * Servers with lower port numbers have higher priority to become the leader.
 */
public class LeaderElection {
    private int myPort;
    private List<Integer> allServerPorts; 
    private FailureDetector failureDetector;
    private int currentLeaderPort;

    public LeaderElection(int myPort, List<Integer> allServerPorts) {
        this.myPort = myPort;
        this.allServerPorts = allServerPorts;
        this.failureDetector = new FailureDetector();
        this.currentLeaderPort = -1;
        
        // Initial election on startup
        electLeader();
    }

    /**
     * Executes the election logic. 
     * The first reachable server in the prioritized list (allServerPorts) is chosen as leader.
     */
    public synchronized void electLeader() {
        for (int port : allServerPorts) {
            if (port == myPort) {
                // If we are the highest priority reachable server, we become the leader
                if (currentLeaderPort != myPort) {
                    currentLeaderPort = myPort;
                    System.out.println("\n[CONSENSUS] I am the new Leader! (Port: " + myPort + ")");
                }
                return;
            } else {
                // Check if a higher priority server is reachable
                if (failureDetector.isServerReachable("localhost", port)) {
                    if (currentLeaderPort != port) {
                        currentLeaderPort = port;
                        System.out.println("\n[CONSENSUS] Server at port " + port + " (Higher Priority) is the Leader.");
                    }
                    return;
                }
            }
        }
    }

    public boolean amILeader() {
        return myPort == currentLeaderPort;
    }
    
    public int getCurrentLeaderPort() {
        return currentLeaderPort;
    }

    public boolean isServerAlive(int port) {
        return failureDetector.isServerReachable("localhost", port);
    }
}

