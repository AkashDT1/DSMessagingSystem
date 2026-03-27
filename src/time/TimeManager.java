package time;

import model.Message;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TimeManager handles system-wide timestamping and synchronization logic.
 * 
 * In distributed systems, clock synchronization is a critical challenge. 
 * This class provides mechanisms for temporal ordering of events (messages)
 * across multiple independent nodes.
 */
public class TimeManager {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * Obtains the current system time in milliseconds for message timestamping.
     * 
     * RATIONALE: We use the system's "physical clock" for global timestamping.
     * While simple, this approach provides a "best-effort" ordering. In a 
     * comprehensive production environment, this would be supplemented by 
     * hybrid logical clocks or NTP-synchronized time for greater precision.
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
     * IMPLEMENTATION DETAIL: To ensure a "Total Ordering", we use the timestamp as 
     * the primary key. If two messages have identical timestamps (due to clock 
     * resolution or simultaneous events), we use the messageId as a secondary 
     * tie-breaker. This ensures that every pair of messages has a deterministic order.
     * 
     * @return A comparator where older messages (smaller timestamps) appear first.
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
