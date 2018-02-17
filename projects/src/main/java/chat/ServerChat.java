package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerChat {
    private static final int PORT = 8887;
    private Map<Agent, Client> chat = new HashMap<>();
    private Queue<Client> queueClients = new LinkedList<>();
    private List<Client> listClients = new LinkedList<>();

    public static void main(String[] args) throws IOException {
        ServerChat server = new ServerChat();
        server.start();
    }

    private void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("Start");
                while (true) {
                    String registration = reader.readLine();
                    if (registration.contains("/register agent ")) {
                        Agent agent = new Agent(socket, this);
                        new Thread(agent).start();
                        chat.put(agent, null);
                        searchChat();
                        break;
                    }
                    if (registration.contains("/register client ")) {
                        Client client = new Client(socket, this);
                        new Thread(client).start();
                        listClients.add(client);
                        searchChat();
                        break;
                    } else {
                        writer.println("Incorrect console command ");
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("logs");
        }
    }

    public synchronized boolean searchChat() {
        for (Map.Entry<Agent, Client> entry : chat.entrySet()) {
            if (queueClients.size() != 0) {
                Agent agent = entry.getKey();
                Client client = entry.getValue();
                if (agent != null && client == null) {
                    Client clientQueue = queueClients.remove();
                    clientQueue.hasAgent = true;
                    chat.put(agent, clientQueue);
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void sendAgentMessage(String message, Agent a) {
        for (Map.Entry<Agent, Client> entry : chat.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (agent == a && client != null)
                client.sendMessage(message);
        }
    }

    public synchronized void sendClientMessage(String message, Client c) {
        for (Map.Entry<Agent, Client> entry : chat.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (client == c)
                agent.sendMessage(message);
        }
    }

    public synchronized void exitClient(Client c) {
        for (Map.Entry<Agent, Client> entry : chat.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (client == c)
                chat.replace(agent, null);
        }
    }

    public synchronized void exitAgent(Agent a) {
        for (Map.Entry<Agent, Client> entry : chat.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (agent == a) {
                listClients.add(client);
                client.hasAgent = false;
                chat.remove(agent, client);
            }
        }
    }

    public synchronized void disconnectClient(Client c) {
        for (Map.Entry<Agent, Client> entry : chat.entrySet()) {
            Client client = entry.getValue();
            if (client == c) {
                listClients.add(client);
                entry.setValue(null);
                return;
            }
        }
    }

    public synchronized void getInQueue(Client client) {
        for (int i = 0; i < listClients.size(); i++) {
            if (client == listClients.get(i)) {
                queueClients.add(client);
                listClients.remove(i);
            }
        }
    }
}
