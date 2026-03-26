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
     * Broadcasts a new message to all other known servers in the cluster.
     * This ensures high availability and consistency across the system.
     * Crucially, we mark the message as a 'replica' before sending it out.
     * This stops the receiving server from broadcasting it again, 
     * which would otherwise cause an infinite network loop.
     * 
     * @param message The original message received from a client.
     */
    public void replicateMessage(Message message) {
        System.out.println("[Replication] Initiating replication for message ID: " + message.getMessageId() + "...");
        
        // Flag to prevent continuous circular replication across the network
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
     * Helper method to send a serializable object to a specific server over a TCP socket.
     * It waits to receive a confirmation (ACK) from the target server to ensure
     * that the message was successfully delivered and processed.
     *
     * @param obj  The data object (like a Message or Sync Request) to send.
     * @param port The port number of the target server.
     */
    private void sendMessageToPort(Object obj, int port) {
        try (Socket socket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send the serialized object over the network
            out.writeObject(obj);
            out.flush();
            
            // Block and wait for a confirmation ACK from the peer before closing
            in.readObject(); 

        } catch (Exception e) {
            // It is normal for peers to occasionally be offline or unavailable. 
            // We just log it and continue so the current server stays active.
            System.err.println("[Replication] Peer server at port " + port + " is currently unreachable. Skipping transmission.");
        }
    }
}
