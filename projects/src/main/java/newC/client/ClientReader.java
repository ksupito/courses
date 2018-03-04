package newC.client;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientReader extends Thread {
    private Socket socket;
    private String registration;
    private String message;
    private static final Logger log = Logger.getLogger(ClientReader.class.getSimpleName());
    boolean is = true;
    BufferedReader keyboard;


    public ClientReader(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
             Scanner scanner = new Scanner(System.in);
             OutputStream sout = socket.getOutputStream();
             DataOutputStream out = new DataOutputStream(sout);) {
            System.out.println("register please");
            while (!socket.isClosed()) {
                registration = keyboard.readLine();
                if (registration.contains("/a") || registration.contains("/c")) {
                    break;
                } else {
                    System.out.println("incorrect command!");
                }
            }
            if (!socket.isClosed()) {
                out.writeUTF(registration);
                out.flush();
            }
            while (!socket.isClosed()) {
                message = keyboard.readLine();
                out.writeUTF(message);
                out.flush();
            }
            return;
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        }
    }

}
