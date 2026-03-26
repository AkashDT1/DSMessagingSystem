package time;

import model.Message;
import java.util.Comparator;

/**
 * TimeManager handles system-wide timestamping and synchronization logic.
 * 
 * In distributed systems, clock synchronization is a critical challenge as 
 * physical clocks on different nodes can drift over time.
 */
public class TimeManager {
    
    /**
     * Obtains the current system time in milliseconds for message timestamping.
     * 
     * NOTE: In our distributed system, this provides the "physical" timestamp.
     * While simple, it relies on system clock accuracy for global ordering.
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
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
        return (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp());
    }
}

