package storage;

import model.Message;
import time.TimeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageStore {
    private List<Message> messages;
    private int port;
    private static final String STORAGE_FILE_PREFIX = "server_storage_";

    public MessageStore(int port) {
        this.port = port;
        this.messages = loadMessagesFromFile();
    }

    private String getFileName() {
        return STORAGE_FILE_PREFIX + port + ".dat";
    }

    public synchronized boolean storeMessage(Message message) {
        for (Message m : messages) {
            if (m.getMessageId().equals(message.getMessageId())) {
                return false;
            }
        }
        messages.add(message);
        saveMessagesToFile();
        return true;
    }

    public synchronized List<Message> getAllMessages() {
        List<Message> sortedMessages = new ArrayList<>(messages);
        Collections.sort(sortedMessages, TimeManager.getTimestampComparator());
        return sortedMessages;
    }

    private synchronized void saveMessagesToFile() {
        try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
                new java.io.FileOutputStream(getFileName()))) {
            out.writeObject(messages);
        } catch (java.io.IOException e) {
            System.err.println("Failed to save messages to file.");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Message> loadMessagesFromFile() {
        java.io.File file = new java.io.File(getFileName());
        if (!file.exists())
            return new ArrayList<>();

        try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
            return (List<Message>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
