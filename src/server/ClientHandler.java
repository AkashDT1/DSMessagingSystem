package server;

import model.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private MessagingServer messagingServer;

    public ClientHandler(Socket socket, MessagingServer messagingServer) {
        this.socket = socket;
        this.messagingServer = messagingServer;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            Object receivedObject = in.readObject();
            if (receivedObject instanceof Message) {
                Message message = (Message) receivedObject;

                if (!message.isReplication()) {
                    System.out.println("New message from client handled.");
                } else {
                    System.out.println("Replicated message from leader handled.");
                }

                messagingServer.processMessage(message);

                out.writeObject("ACK");
                out.flush();
            } else if (receivedObject instanceof String && receivedObject.equals("SYNC_REQUEST")) {
                System.out.println("Sync request received. Sending all local messages.");
                out.writeObject(messagingServer.getMessageStore().getAllMessages());
                out.flush();
            }
        } catch (Exception e) {
            // Socket closed or connection dropped silently
        }
    }
}
