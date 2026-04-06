package model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a basic text message in the Distributed Messaging System.
 * Ensures consistent serialization across client and server nodes.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageId;
    private String sender;
    private String receiver;
    private String content;
    private long timestamp;
    
    // Indicates if the message originated from a cross-server replication sync.
    // This flag is critical for preventing infinite network loops during broadcasting.
    private boolean isReplication; 

    public Message(String sender, String receiver, String content, long timestamp) {
        // Unique ID ensures we can track duplicates across the distributed system
        this.messageId = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
        this.isReplication = false; // Default false; only replicas flip this flag
    }

    public String getMessageId() { return messageId; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public boolean isReplication() { return isReplication; }
    public void setReplication(boolean replication) { isReplication = replication; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(messageId, message.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }
}
