package chat.classes;

import chat.server.ServerChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UserThread implements Runnable {
    private Socket socket;
    private ServerChat server;
    BufferedReader reader;
    PrintWriter writer;
    public String name;

    public UserThread(Socket socket, ServerChat server, String name) {
        this.socket = socket;
        this.server = server;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                String registration = reader.readLine();
                if (registration.contains("/register agent ")) {
                    String name = registration.replaceFirst("/register agent ", "");
                    Agent agent = new Agent(socket, server, name);
                    server.addAgent(agent);
                    agent.start();
                    server.searchChat();

                    break;
                }
                if (registration.contains("/register client ")) {
                    String name = registration.replaceFirst("/register client ", "");
                    Client client = new Client(socket, server, name);
                    server.addClient(client);
                    client.start();
                    server.searchChat();

                    break;
                } else {
                    writer.println("Incorrect console command ");
                }
            }
        } catch (IOException e) {
        }
    }
}
