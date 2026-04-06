package fault;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FailureDetector {
    
    // now we handle node recovery better
    public boolean checkNode(String host, int port) {
        if (port < 0) return false;

        int retries = 3;
        for (int i = 0; i < retries; i++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 2000);
                
                // if it failed before and now succeeds, it's recovered
                if (i > 0) {
                    nodeRecovered(port);
                }
                return true;
            } catch (IOException e) {
                System.out.println("trying again... " + port);
                try { Thread.sleep(500); } catch (Exception ex) {}
            }
        }
        System.err.println("NODE DOWN: server " + port + " not responding at all!");
        return false;
    }

    // actual logic for recovery
    public void nodeRecovered(int port) {
        if (port > 0) {
            System.out.println("RECOVERY: node " + port + " is officially back up");
        }
    }
}
