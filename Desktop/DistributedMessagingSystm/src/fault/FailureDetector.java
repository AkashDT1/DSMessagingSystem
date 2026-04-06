package fault;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FailureDetector {
    
    // improving retry logic with small wait
    public boolean checkNode(String host, int port) {
        int retries = 3;
        for (int i = 0; i < retries; i++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 2000);
                return true;
            } catch (IOException e) {
                System.out.println("node at " + port + " not responding, trying after 500ms...");
                try { Thread.sleep(500); } catch (InterruptedException ie) {}
            }
        }
        System.err.println("NODE DOWN: server " + port + " really not working!");
        return false;
    }
}
