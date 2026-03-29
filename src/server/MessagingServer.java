package server;

import consensus.LeaderElection;
import model.Message;
import replication.ReplicationManager;
import storage.MessageStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MessagingServer {
    private MessageStore messageStore;
    private ReplicationManager replicationManager;
    private LeaderElection leaderElection;
    private ServerConnectionManager connectionManager;
    private int myPort;
    private List<Integer> allServerPorts;

    public MessagingServer(int myPort, List<Integer> allServerPorts) {
        this.myPort = myPort;
        this.allServerPorts = allServerPorts;
        this.messageStore = new MessageStore(myPort);

        List<Integer> otherPorts = new ArrayList<>(allServerPorts);
        otherPorts.remove(Integer.valueOf(myPort));

        this.replicationManager = new ReplicationManager(otherPorts, this);
        this.leaderElection = new LeaderElection(myPort, allServerPorts);
        this.connectionManager = new ServerConnectionManager(myPort, this);
    }

    public void startServer() {
        connectionManager.start();

        // Data Management/Recovery: Always sync from available neighbors on startup to ensure consistency
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Warm-up period for connection manager
                System.out.println("[RECOVERY] Checking with cluster neighbors to catch up on missed messages...");
                for (int otherPort : allServerPorts) {
                    if (otherPort != myPort && leaderElection.isServerAlive(otherPort)) {
                        System.out.println("[RECOVERY] Synchronizing state with peer at port: " + otherPort);
                        replicationManager.requestSyncFrom(otherPort);
                        break; // Consistent state achieved after first successful sync
                    }
                }
            } catch (InterruptedException e) {
            }
        }).start();

        // Heartbeat Monitor: Periodically re-evaluates the cluster state to handle potential node failures or joins.
        // This keeps the leader role up-to-date and avoids split-brain behavior.
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Heartbeat frequency (5 seconds)
                    leaderElection.electLeader(); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void processMessage(Message message) {
        // 1. Persist the message locally if it is not already stored
        boolean isNewMessage = messageStore.storeMessage(message);

        if (isNewMessage) {
            String type = message.isReplication() ? "REPLICATION" : "NEW";
            System.out.println("[" + type + "] Committed message from " + message.getSender() + ": " + message.getContent());
        }

        // 2. COORDINATION LOGIC: Actions differ based on the node's cluster role (Leader or Follower)
        if (isNewMessage && !message.isReplication()) {
            if (leaderElection.amILeader()) {
                // If this node is the LEADER, it must coordinate replication across all other nodes.
                // This ensures that all messages are distributed consistently within the cluster.
                System.out.println("[COORDINATION] Active Leader Role: Distributing message sequence to the cluster.");
                replicationManager.replicateMessage(message);
            } else {
                // If this node is a FOLLOWER, it cannot replicate the message itself.
                // Instead, it forwards the request to the current Leader for centralized coordination.
                int leader = leaderElection.getCurrentLeaderPort();
                System.out.println("[COORDINATION] Follower Role: Forwarding message to coordinator at port " + leader);
                replicationManager.forwardToLeader(message, leader);
            }
        }
    }

    public MessageStore getMessageStore() {
        return messageStore;
    }

    public void showAllMessages() {
        List<Message> messages = messageStore.getAllMessages();
        
        System.out.println("\n===== CLUSTER COORDINATION STATUS =====");
        System.out.println("Node: " + myPort + (leaderElection.amILeader() ? " [ACTIVE_COORDINATOR]" : " [FOLLOWER]"));
        System.out.println("Coordinator: " + (leaderElection.amILeader() ? "Myself (Responsible for replication)" : "Port " + leaderElection.getCurrentLeaderPort()));
        System.out.println("Follower Role: " + (!leaderElection.amILeader() ? "Forwarding client requests to coordinator" : "Serving direct requests and coordinating replication"));
        System.out.println("-------------------------------------");
        if (messages.isEmpty()) {
            System.out.println("No messages stored yet in the cluster.");
        } else {
            for (Message m : messages) {
                System.out.println("[" + m.getSender() + "]: " + m.getContent());
            }
        }
        System.out.println("=======================================\n");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter port to start this server (e.g. 5001, 5002, 5003): ");
        int port = Integer.parseInt(scanner.nextLine());

        List<Integer> allPorts = Arrays.asList(5001, 5002, 5003);
        MessagingServer server = new MessagingServer(port, allPorts);
        server.startServer();

        System.out.println("Server is running. Options: 'view' to see messages, 'quit' to stop.");
        while (true) {
            String command = scanner.nextLine().trim().toLowerCase();
            if (command.equals("view")) {
                server.showAllMessages();
            } else if (command.equals("quit")) {
                scanner.close();
                System.exit(0);
            }
        }
    }
}
