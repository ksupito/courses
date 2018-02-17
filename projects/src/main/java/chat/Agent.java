package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Agent implements Runnable, User {
    private Socket socket;
    private ServerChat server;
    private PrintWriter writer;

    public Agent(Socket socket, ServerChat server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                String message = reader.readLine();
                if (message.equals("/exit")) {
                    server.exitAgent(this);
                    server.searchChat();
                    socket.close();
                    break;
                }
                server.sendAgentMessage(message, this);
            }
        } catch (IOException e) {
            System.out.println("logs");
        }
    }

    public void sendMessage(String message) {
        writer.println("client: " + message);
    }
}
