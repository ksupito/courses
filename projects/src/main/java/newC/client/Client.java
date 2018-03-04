package newC.client;

import newC.server.ServerThread;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static final Logger log = Logger.getLogger(Client.class.getSimpleName());
    int count = 100;
    ClientReader clientReader;

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.startClient();
    }

    private synchronized void startClient() throws IOException {

        try (Socket socket = new Socket(InetAddress.getLocalHost(), 1112);
             InputStream sin = socket.getInputStream();
             DataInputStream in = new DataInputStream(sin)) {
            clientReader = new ClientReader(socket);
            clientReader.start();
            String message;
            while (!socket.isClosed()) {
                message = in.readUTF();
                if (message.equals("cancel")) {
                    socket.close();
                    break;
                }
                System.out.println(message);
            }
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Connection is failed");
            log.error(e.getStackTrace());
            System.out.println("Sorry, the server is not available. Please try later.");
            System.exit(0);

        }
    }
}
