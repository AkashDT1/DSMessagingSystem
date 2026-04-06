package fault;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FailureDetector {
    
    // basic retry if node not responding
    public boolean checkNode(String host, int port) {
        int retries = 2;
        for (int i = 0; i < retries; i++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 2000);
                return true;
            } catch (IOException e) {
                // try once more
                System.out.println("trying to connect to " + port + " again...");
            }
        }
        System.err.println("NODE DOWN: server " + port + " really not working!");
        return false;
    }
}
