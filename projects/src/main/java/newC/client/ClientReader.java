package newC.client;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ClientReader extends Thread {
    private Socket socket;
    private String registration;
    private String message;
    private static final Logger log = Logger.getLogger(ClientReader.class.getSimpleName());

    public ClientReader(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
             OutputStream sout = socket.getOutputStream();
             DataOutputStream out = new DataOutputStream(sout);) {
            System.out.println("register please");
            while (true) {
                registration = keyboard.readLine();
                if (registration.contains("/a") || registration.contains("/c")) {
                    break;
                } else {
                    System.out.println("incorrect command!");
                }
            }
            out.writeUTF(registration);
            out.flush();
            while (!socket.isClosed() ) {
                message = keyboard.readLine();
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
