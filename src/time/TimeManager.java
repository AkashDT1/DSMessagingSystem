package time;

import model.Message;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TimeManager handles system-wide timestamping and synchronization logic.
 * 
 * In distributed systems, clock synchronization is a critical challenge as 
 * physical clocks on different nodes can drift over time. This class provides
 * the foundation for message ordering based on coordinated timestamps.
 */
public class TimeManager {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * Obtains the current system time in milliseconds for message timestamping.
     * 
     * NOTE: In our distributed system, this provides the "physical" timestamp.
     * While simple, it relies on system clock accuracy for global ordering.
     * In a production environment, this would be supplemented by NTP or 
     * logical clocks like Lamport timestamps.
     * 
     * @return The current system time in milliseconds since epoch.
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
    
    /**
     * Formats a long timestamp into a human-readable string.
     * Useful for debugging and presenting message flows in the UI/Logs.
     * 
     * @param timestamp The timestamp in milliseconds.
     * @return A formatted string (HH:mm:ss.SSS).
     */
    public static String getFormattedTimestamp(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Provides a comparator to order messages based on their timestamps.
     * This establishes a "Total Ordering" of messages across the distributed network.
     * 
     * Such ordering ensures that all recipients see messages in a consistent sequence,
     * which is critical for maintaining a coherent state in the messaging system.
     * 
     * @return A comparator where smaller timestamps (older messages) come first.
     */
    public static Comparator<Message> getTimestampComparator() {
        return Comparator.comparingLong(Message::getTimestamp);
    }
}


