package fault;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FailureDetector {
    
    // now we can see some logs for failure
    public boolean checkNode(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(2000);
            socket.connect(new InetSocketAddress(host, port), 2000);
            return true;
        } catch (IOException e) {
            // node down so print something
            System.err.println("NODE DOWN: server at port " + port + " is not replying");
            return false;
        }
    }
}
