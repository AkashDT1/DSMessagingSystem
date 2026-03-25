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
     * Gets the current system time in milliseconds.
     * 
     * NOTE: This relies on the local system's physical clock. In a production 
     * distributed environment, this should ideally be combined with logical 
     * clocks (Lamport or Vector) to ensure causal ordering.
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
    
    /**
     * Provides a comparator to order messages based on their timestamps.
     * This establishes a total order based on the time the message was created.
     */
    public static Comparator<Message> getTimestampComparator() {
        return (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp());
    }
}

