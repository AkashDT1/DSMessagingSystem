package model;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageId;
    private String sender;
    private String receiver;
    private String content;
    private long timestamp;
    private boolean isReplication; // Flag to indicate if this is a replication sync

    public Message(String sender, String receiver, String content, long timestamp) {
        this.messageId = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
        this.isReplication = false; 
    }

    public String getMessageId() { return messageId; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public boolean isReplication() { return isReplication; }
    public void setReplication(boolean replication) { isReplication = replication; }
}
