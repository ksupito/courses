package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable , User{
    private Socket socket;
    private ServerChat server;
    private PrintWriter writer;
    boolean hasAgent;
    boolean waitAgent;

    public Client(Socket socket, ServerChat server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            hasAgent = false;
            waitAgent = false;
            while (true) {
                String message = reader.readLine();
                if (!hasAgent && !waitAgent) {
                    server.getInQueue(this);
                    waitAgent = true;
                    checkAgent(message);
                } else if (!hasAgent && waitAgent) {
                    checkAgent(message);
                } else if (hasAgent) {
                    if (message.equals("/exit")) {
                        server.exitClient(this);
                        server.searchChat();
                        socket.close();
                        break;
                    }
                    if (message.equals("/leave")) {
                        hasAgent = false;
                        waitAgent = false;
                        server.disconnectClient(this);
                        server.searchChat();
                    } else {
                        server.sendClientMessage(message, this);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("logs");
        }
    }

    public void sendMessage(String message) {
        writer.println("agent: " + message);
    }

    public void checkAgent(String message) {
        boolean isChat = server.searchChat();
        if (!isChat) {
            sendMessage("There aren't free agents in this moment");
        } else if (isChat) {
            server.sendClientMessage(message, this);
            hasAgent = true;
        }

    }
}
