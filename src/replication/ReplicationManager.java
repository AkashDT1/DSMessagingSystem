package replication;

import model.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ReplicationManager {
    private List<Integer> otherServerPorts;
    private server.MessagingServer messagingServer;

    public ReplicationManager(List<Integer> otherServerPorts, server.MessagingServer messagingServer) {
        this.otherServerPorts = otherServerPorts;
        this.messagingServer = messagingServer;
    }

    public void replicateMessage(Message message) {
        System.out.println("Replicating message to other servers");
        message.setReplication(true); // Flag to stop circular replication

        for (int targetPort : otherServerPorts) {
            sendMessageToPort(message, targetPort);
        }
    }

    public void forwardToLeader(Message message, int leaderPort) {
        sendMessageToPort(message, leaderPort);
    }

    public void requestSyncFrom(int leaderPort) {
        if (leaderPort == -1)
            return;
        System.out.println("Syncing messages from current leader at port " + leaderPort);
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
            System.out.println("Sync failed. Moving on with local state.");
        }
    }

    private void sendMessageToPort(Object obj, int port) {
        try (Socket socket = new Socket("localhost", port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(obj);
            out.flush();
            in.readObject(); // Wait for confirmation ACK

        } catch (Exception e) {
            int serverId = port - 5000;
            System.out.println("Server at port " + port + " unreachable. Skipping message.");
        }
    }
}
