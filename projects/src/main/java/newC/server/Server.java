package newC.server;

import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final Logger log = Logger.getLogger(Server.class.getSimpleName());

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(1111)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(socket);
                new Thread(serverThread).start();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}


