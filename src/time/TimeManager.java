package time;

import model.Message;
import java.util.Comparator;

public class TimeManager {
    
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
    
    /*
     * CLOCK SYNCHRONIZATION NOTE:
     * In a real distributed system, physical clocks on different servers may drift and 
     * are never perfectly synchronized. If Server A's clock is 2 seconds ahead of Server B,
     * a message sent from A might incorrectly appear to happen "after" a message from B, 
     * affecting the sorted order. 
     * 
     * To solve this properly, distributed systems use Logical Clocks (like Lamport 
     * timestamps or Vector Clocks) to establish correct causality rather than 
     * relying purely on local System time.
     */
    public static Comparator<Message> getTimestampComparator() {
        return new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                return Long.compare(m1.getTimestamp(), m2.getTimestamp());
            }
        };
    }
}
