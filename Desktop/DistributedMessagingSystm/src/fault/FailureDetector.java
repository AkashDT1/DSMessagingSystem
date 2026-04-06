package fault;

import java.io.IOException;
import java.net.Socket;

public class FailureDetector {
    
    // basic ping logic started
    public boolean checkPing(String host, int port) {
        try {
            Socket s = new Socket(host, port);
            s.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
