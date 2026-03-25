package replication;

import model.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Manages all peer-to-peer data replication tasks between messaging servers.
 * Responsible for forwarding client requests and synchronizing state across cluster nodes.
 */
public class ReplicationManager {
    private List<Integer> otherServerPorts;
    private server.MessagingServer messagingServer;

    public ReplicationManager(List<Integer> otherServerPorts, server.MessagingServer messagingServer) {
        this.otherServerPorts = otherServerPorts;
        this.messagingServer = messagingServer;
    }

    /**
     * Broadcasts the message to all known peer servers to ensure data redundancy.
     * Sets the replication flag to true to prevent an infinite loop where peers replicate back to the sender.
     * 
     * @param message The message to replicate.
     */
    public void replicateMessage(Message message) {
        System.out.println("Replicating message to other peers...");
        message.setReplication(true); // Flag to stop circular replication

        for (int targetPort : otherServerPorts) {
            sendMessageToPort(message, targetPort);
        }
    }

    /**
     * Forwards a client request directly to the leader node.
     * 
     * @param message    The raw message from the client.
     * @param leaderPort The port of the designated cluster leader.
     */
    public void forwardToLeader(Message message, int leaderPort) {
        sendMessageToPort(message, leaderPort);
    }

    /**
     * Synchronizes missing messages from the current leader during startup.
     * Brings this node up to speed if it joins late or recovers from a crash.
     * 
     * @param leaderPort Port of the current elected leader.
     */
    public void requestSyncFrom(int leaderPort) {
        if (leaderPort == -1) {
            return;
        }
        
        System.out.println("Syncing missed messages from current leader at port " + leaderPort + "...");
        try (Socket socket = new Socket("localhost", leaderPort);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("SYNC_REQUEST");
            out.flush();

            Object received = in.readObject();
            if (received instanceof List) {
                @SuppressWarnings("unchecked")
                List<Message> allMessages = (List<Message>) received;
                for (Message m : allMessages) {
                    messagingServer.processMessage(m);
                }
                System.out.println("Synchronization complete: " + allMessages.size() + " messages synchronized.");
            }
        } catch (Exception e) {
            System.err.println("Leader sync failed at port " + leaderPort + ". " + e.getMessage() + ". Moving on with local state.");
        }
    }

    /**
     * Helper method to send a serializable object to a specific server over a TCP socket.
     * Awaits a confirmation ACK to ensure delivery.
     */
    private void sendMessageToPort(Object obj, int port) {
        try (Socket socket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(obj);
            out.flush();
            in.readObject(); // Wait for confirmation ACK

        } catch (Exception e) {
            // Log unreachable peers, server operations continue normally
            System.err.println("Peer server at port " + port + " unreachable. Skipping message transmission.");
        }
    }
}
