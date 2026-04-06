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
     * This guarantees data redundancy and state consistency across the distributed system.
     * 
     * **Replication Flow & Loop Prevention:**
     * Before sending to followers, the replication flag is explicitly set to true.
     * Followers receiving a replicated message will store it but will NOT re-broadcast it.
     * This prevents infinite replication loops and network flooding.
     * 
     * @param message The original message to replicate.
     */
    public void replicateMessage(Message message) {
        // need to mark this as replica so it doesnt loop
        message.setReplication(true); 
        System.out.println("broadcasting message " + message.getMessageId() + " to all nodes");
        if (otherServerPorts == null || otherServerPorts.isEmpty()) {
            System.out.println("no ports to replicate to");
            return;
        }

        for (int targetPort : otherServerPorts) {
            System.out.println("Sending replica to port: " + targetPort);
            sendToNode(message, targetPort);
        }
    }

    /**
     * Forwards a client request directly to the leader node.
     * 
     * @param message    The raw message from the client.
     * @param leaderPort The port of the designated cluster leader.
     */
    public void forwardToLeader(Message message, int leaderPort) {
        System.out.println("forwarding the message to leader at port " + leaderPort + " now");
        sendToNode(message, leaderPort);
    }

    /**
     * Synchronizes missing messages from the current leader during startup.
     * This state transfer is crucial for node recovery or joining a running cluster,
     * ensuring the requesting node achieves eventual consistency with the leader.
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

            // asking the leader for the messages we missed
            out.writeObject("SYNC_REQUEST");
            out.flush();

            // wait for the list of messages
            Object received = in.readObject();
            if (received == null) {
                System.out.println("no messages received from sync");
                return;
            }
            if (received instanceof List) {
                @SuppressWarnings("unchecked")
                List<Message> allMessages = (List<Message>) received;
                for (Message m : allMessages) {
                    m.setReplication(true); // stop loop
                    messagingServer.processMessage(m);
                }
                System.out.println("synced " + allMessages.size() + " messages");
            }
        } catch (java.net.ConnectException ce) {
            System.err.println("couldnt connect to leader for syncing");
        } catch (Exception e) {
            System.err.println("leader sync failed because: " + e.getMessage());
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
    private void sendToNode(Object obj, int port) {
        try (Socket socket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("sending to port " + port);
            out.writeObject(obj);
            out.flush();
            System.out.println("waiting for ack from " + port);
            
            in.readObject(); 
            System.out.println("got ack from " + port);

        } catch (Exception e) {
            System.err.println("failed to send to port " + port + ": " + e.getMessage());
        }
    }
}
