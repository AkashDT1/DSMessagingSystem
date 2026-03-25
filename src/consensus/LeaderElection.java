package consensus;

import fault.FailureDetector;
import java.util.List;

public class LeaderElection {
    private int myServerId;
    private List<Integer> allServerPorts; 
    private FailureDetector failureDetector;
    private int currentLeaderPort;

    public LeaderElection(int myServerId, List<Integer> allServerPorts) {
        this.myServerId = myServerId;
        this.allServerPorts = allServerPorts;
        this.failureDetector = new FailureDetector();
        this.currentLeaderPort = -1;
        electLeader();
    }

    // Priority-based algorithm
    public synchronized void electLeader() {
        for (int port : allServerPorts) {
            if (port == myServerId) {
                if (currentLeaderPort != myServerId) {
                    currentLeaderPort = myServerId;
                    System.out.println("\n*** I am the new Leader! (Port: " + myServerId + ") ***");
                }
                return;
            } else {
                if (failureDetector.isServerReachable("localhost", port)) {
                    if (currentLeaderPort != port) {
                        currentLeaderPort = port;
                        System.out.println("\n*** Server at port " + port + " is the Leader. ***");
                    }
                    return;
                }
            }
        }
    }

    public boolean amILeader() {
        return myServerId == currentLeaderPort;
    }
    
    public int getCurrentLeaderPort() {
        return currentLeaderPort;
    }

    public boolean isServerAlive(int port) {
        return failureDetector.isServerReachable("localhost", port);
    }
}
