package time;

import model.Message;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;

// handle all time and sync things
public class TimeManager {
    
    // helper to create message with time
    public static Message createWithTime(String sender, String receiver, String content) {
        return new Message(sender, receiver, content, getCurrentTime());
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    // gets current system time
    public static long getCurrentTime() {
        // get current system time mills
        return System.currentTimeMillis();
    }
    
    /**
     * Formats a long timestamp into a human-readable string.
     * 
     * @param timestamp The timestamp in milliseconds.
     * @return A formatted string (HH:mm:ss.SSS) for UI and log visibility.
     */
    public static String getFormattedTimestamp(long timestamp) {
        // format time for display
        return DATE_FORMAT.format(new Date(timestamp));
    }
    
    // comparator to sort messages by timestamp
    public static Comparator<Message> getTimestampComparator() {
        return (m1, m2) -> {
            // if time is same we handle the issue using IDs
            if (m1.getTimestamp() == m2.getTimestamp()) {
                int res = m1.getMessageId().compareTo(m2.getMessageId());
                if (res == 0) {
                    // last fallback sorting
                    return m1.getSender().compareTo(m2.getSender());
                }
                return res;
            }
            return Long.compare(m1.getTimestamp(), m2.getTimestamp());
        };
    }

    // compare two messages easily
    public static int compare(Message m1, Message m2) {
        if (m1 == null || m2 == null) return 0; // quick fix for null
        return getTimestampComparator().compare(m1, m2);
    }
}
