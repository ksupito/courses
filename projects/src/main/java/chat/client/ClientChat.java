package chat.client;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.Socket;

public class ClientChat {
    private static final Logger log = Logger.getLogger(Reader.class.getSimpleName());

    public static void main(String[] args) {
        ClientChat client = new ClientChat();
        try {
            client.start();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private void start() throws IOException {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 8887)) {
        }
    }
}
