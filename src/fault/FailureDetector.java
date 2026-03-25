package fault;

import java.io.IOException;
import java.net.Socket;

public class FailureDetector {
    
    // Simple failure detection: attempt to open a socket connection
    // If it throws an exception, the server is unreachable/failed.
    public boolean isServerReachable(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
