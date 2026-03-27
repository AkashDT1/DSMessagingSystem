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
     * Broadcasts a new message to all peer servers in the cluster.
     * This ensures data redundancy and consistency across the distributed system.
     * 
     * The replication flag is set to true before broadcasting.
     * This prevents infinite replication loops, as receiving nodes will not re-broadcast it.
     * 
     * @param message The original message to replicate.
     */
    public void replicateMessage(Message message) {
        System.out.println("[Replication] Initiating replication for message ID: " + message.getMessageId() + "...");
        
        // Mark message as a replica to prevent infinite circular replication
        message.setReplication(true); 

        for (int targetPort : otherServerPorts) {
            System.out.println("[Replication] -> Sending replica to peer at port " + targetPort);
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
        System.out.println("Forwarding client request to leader at port " + leaderPort + "...");
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
                    m.setReplication(true); // Prevent re-replication of synced messages locally
                    messagingServer.processMessage(m);
                }
                System.out.println("Synchronization complete: " + allMessages.size() + " messages synchronized.");
            }
        } catch (Exception e) {
            System.err.println("Leader sync failed at port " + leaderPort + ". " + e.getMessage() + ". Moving on with local state.");
        }
    }

    /**
     * Sends a serializable object to a target server over a TCP socket.
     * Blocks until an acknowledgment (ACK) is received from the target,
     * ensuring successful delivery.
     *
     * @param obj  The object (Message or Sync Request) to send.
     * @param port The target server port.
     */
    private void sendMessageToPort(Object obj, int port) {
        try (Socket socket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Transmit the serialized object
            out.writeObject(obj);
            out.flush();
            
            // Wait for ACK confirmation from the target node
            in.readObject(); 

        } catch (Exception e) {
            // Log connection failures without crashing, allowing the system to remain available
            System.err.println("[Replication] Peer server at port " + port + " is currently unreachable. Skipping transmission.");
        }
    }
}
