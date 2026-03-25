package storage;

import model.Message;
import time.TimeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles persistent storage of messaging data.
 * Messages are stored in a local `.dat` file to recover state on restart.
 */
public class MessageStore {
    private List<Message> messages;
    private Set<String> seenMessageIds;
    private int port;
    
    private static final String STORAGE_FILE_PREFIX = "server_storage_";

    public MessageStore(int port) {
        this.port = port;
        this.messages = loadMessagesFromFile();
        
        // Populate the lookup set for O(1) deduplication
        this.seenMessageIds = new HashSet<>();
        for (Message m : this.messages) {
            this.seenMessageIds.add(m.getMessageId());
        }
    }

    private String getFileName() {
        return STORAGE_FILE_PREFIX + port + ".dat";
    }

    /**
     * Stores a new message locally and persists the state.
     * Deduplicates incoming messages using their unique UUID to prevent double-writes
     * that could arise from overlapping replication flows.
     *
     * @param message The message to store.
     * @return true if stored successfully, false if the message already exists.
     */
    public synchronized boolean storeMessage(Message message) {
        // O(1) deduplication check using HashSet. If add() returns false, it was already present.
        if (!seenMessageIds.add(message.getMessageId())) {
            return false;
        }
        
        messages.add(message);
        saveMessagesToFile();
        return true;
    }

    /**
     * Retrieves all stored messages, sorted chronologically.
     * Creates a defensive copy to prevent concurrent modification issues.
     *
     * @return A time-ordered list of messages.
     */
    public synchronized List<Message> getAllMessages() {
        // Defensive copy allows safe iteration and sorting
        List<Message> sortedMessages = new ArrayList<>(messages);
        Collections.sort(sortedMessages, TimeManager.getTimestampComparator());
        return sortedMessages;
    }

    /**
     * Serializes the current list of messages to the local disk.
     */
    private synchronized void saveMessagesToFile() {
        try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
                new java.io.FileOutputStream(getFileName()))) {
            out.writeObject(messages);
        } catch (java.io.IOException e) {
            System.err.println("Failed to save messages to file: " + getFileName());
        }
    }

    /**
     * Deserializes existing messages from disk during startup.
     * 
     * @return The loaded list of messages, or an empty list if none exist or an error occurs.
     */
    @SuppressWarnings("unchecked")
    private List<Message> loadMessagesFromFile() {
        java.io.File file = new java.io.File(getFileName());
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
            return (List<Message>) in.readObject();
        } catch (Exception e) {
            System.err.println("Warning: Could not load initial messages from " + getFileName());
            return new ArrayList<>();
        }
    }
}
