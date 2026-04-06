package fault;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FailureDetector {
    
    // adding timeout to check if node down
    public boolean checkNode(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 2000);
            return true;
        } catch (IOException e) {
            // failed so node is down
            return false;
        }
    }
}
