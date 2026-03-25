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

        // Recovery Logic: Always try to sync from any available server on startup
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Wait for connection manager to start
                System.out.println("Checking with neighbors to catch up on missed messages...");
                for (int otherPort : allServerPorts) {
                    if (otherPort != myPort && leaderElection.isServerAlive(otherPort)) {
                        replicationManager.requestSyncFrom(otherPort);
                        break; // Stop after first successful sync attempt
                    }
                }
            } catch (InterruptedException e) {
            }
        }).start();

        // Background fault-tolerance thread to perform heartbeat / leader election
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    leaderElection.electLeader();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void processMessage(Message message) {
        // 1. Store locally if it's new
        boolean isNewMessage = messageStore.storeMessage(message);

        if (isNewMessage) {
            String type = message.isReplication() ? "REPLICATION" : "NEW";
            System.out
                    .println("[" + type + "] Stored message from " + message.getSender() + ": " + message.getContent());
        }

        // 2. Replication Logic Coordinated By Leader
        if (isNewMessage && !message.isReplication()) {
            if (leaderElection.amILeader()) {
                replicationManager.replicateMessage(message);
            } else {
                // Not the leader, forward up to the leader so it manages the system replication
                System.out.println("Forwarding message to leader to handle replication.");
                replicationManager.forwardToLeader(message, leaderElection.getCurrentLeaderPort());
            }
        }
    }

    public MessageStore getMessageStore() {
        return messageStore;
    }

    public void showAllMessages() {
        List<Message> messages = messageStore.getAllMessages();
        System.out.println("\n===== ALL MESSAGES ON THIS SERVER =====");
        if (messages.isEmpty()) {
            System.out.println("No messages stored yet.");
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
