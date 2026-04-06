package time;

import model.Message;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TimeManager handles system-wide timestamping and synchronization logic.
 * 
 * In distributed systems, establishing a consistent timeline is a critical challenge. 
 * This class provides a centralized mechanism for temporal ordering of events 
 * (messages) across multiple independent nodes, using physical clock synchronization.
 */
public class TimeManager {
    
    // adding timestamp to message
    public static Message createWithTime(String sender, String receiver, String content) {
        return new Message(sender, receiver, content, getCurrentTime());
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * Obtains the current system time in milliseconds for message timestamping.
     * 
     * RATIONALE: We use the system's "physical clock" for global timestamping.
     * While simple, this approach provides a "best-effort" ordering. In a 
     * robust production environment, this would be supplemented by techniques 
     * like hybrid logical clocks (HLC) or NTP synchronization for higher precision.
     * 
     * @return The current system time in milliseconds since epoch.
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
    
    /**
     * Formats a long timestamp into a human-readable string.
     * 
     * @param timestamp The timestamp in milliseconds.
     * @return A formatted string (HH:mm:ss.SSS) for UI and log visibility.
     */
    public static String getFormattedTimestamp(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Provides a comparator to order messages based on their timestamps.
     * 
     * IMPLEMENTATION DETAIL: To achieve "Total Ordering" in a distributed environment, 
     * we use the timestamp as the primary key. If two messages have identical 
     * timestamps (due to clock resolution limits), the messageId serves as a 
     * secondary tie-breaker. Assuming unique IDs, this guarantees a deterministic, 
     * consistent order across all nodes in the system.
     * 
     * @return A comparator where older messages (smaller timestamps) precede newer ones.
     */
    public static Comparator<Message> getTimestampComparator() {
        return Comparator
                .comparingLong(Message::getTimestamp)
                .thenComparing(Message::getMessageId);
    }

    /**
     * Direct comparison of two messages to determine their relative order.
     * Useful for checking if message A should precede message B.
     * 
     * @return result < 0 if m1 is older, > 0 if m2 is older, 0 if identical.
     */
    public static int compare(Message m1, Message m2) {
        return getTimestampComparator().compare(m1, m2);
    }
}
