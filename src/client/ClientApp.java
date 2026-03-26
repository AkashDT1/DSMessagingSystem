package client;

import model.Message;
import time.TimeManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter server port to connect to (e.g., 5001): ");
        int port = Integer.parseInt(scanner.nextLine());

        try (Socket socket = new Socket("localhost", port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Connected to server at port " + port);

            System.out.print("\nEnter your name: ");
            String sender = scanner.nextLine();

            System.out.print("\nEnter message: ");
            String content = scanner.nextLine();

            // 1. Generate a timestamp to establish message ordering
            // This allows the system to sort messages globally across all users.
            long messageTimestamp = TimeManager.getCurrentTime();
            String timeStr = TimeManager.getFormattedTimestamp(messageTimestamp);
            
            // 2. Wrap the sender details and content into a structured message object. 
            // The timestamp is key for ensuring total message order in the system.
            Message message = new Message(sender, "All", content, messageTimestamp);
            System.out.println("Preparing to send message at [" + timeStr + "]");

            // Transmit the message for processing and distribution
            out.writeObject(message);
            out.flush();

            // Receive ack
            String confirmation = (String) in.readObject();
            System.out.println("\n" + confirmation);

        } catch (Exception e) {
            System.out.println("Error connecting to server. Is it running on this port?");
        } finally {
            scanner.close();
        }
    }
}
