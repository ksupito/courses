package chat.server;

import chat.classes.Agent;
import chat.classes.UserThread;
import chat.classes.Client;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerChat {
    private static final Logger log = Logger.getLogger(Reader.class.getSimpleName());
    private static final int PORT = 8887;
    private Map<Agent, Client> mapAgents = new HashMap<>();
    private Queue<Client> queueClients = new LinkedList<>();
    private List<Client> listClients = new LinkedList<>();
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    public static void main(String[] args) {
        org.apache.log4j.PropertyConfigurator.configure("src/main/resources/log4j.properties");
        ServerChat server = new ServerChat();
        try {
            server.start();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                socket = serverSocket.accept();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("Start");
                UserThread classUser = new UserThread(socket, this, "n");
                new Thread(classUser).start();
            }
        } finally {
            if (socket != null || reader != null || writer != null) {
                socket.close();
                reader.close();
                writer.close();
            }
        }
    }

    public synchronized void addAgent(Agent agent) {
        mapAgents.put(agent, null);
    }

    public synchronized void addClient(Client client) {
        listClients.add(client);
    }

    public synchronized boolean searchChat() { //search new chat for an free agent and a client without client
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            if (queueClients.size() != 0) {
                Agent agent = entry.getKey();
                Client client = entry.getValue();
                if (agent != null && client == null) {
                    Client clientQueue = queueClients.remove();
                    clientQueue.hasAgent = true;
                    mapAgents.put(agent, clientQueue);
                    agent.sendMessage("new chat was started", "chat");
                    clientQueue.sendMessage("new chat was started", "chat");
                    clientQueue.checkMessage();
                    log.info("New chat was started");
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void sendAgentMessage(String message, Agent a) { //method send message to client
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (agent == a && client != null) {
                client.sendMessage(message, agent.name);
                log.info("Message to a client");
                return;
            }
        }
    }

    public synchronized void sendClientMessage(String message, Client c) { //method send message to agent
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (client == c) {
                agent.sendMessage(message, client.name);
                log.info("Message to an agent");
                return;
            }
        }
    }

    public synchronized void exitClient(Client c) { //if a client input /exit in time of a chat method'll remove the client from map
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (client == c) {
                agent.sendMessage("chat was ended", "chat");
                mapAgents.replace(agent, null);
                log.info("Client exited");
                return;
            }
        }
    }

    public synchronized void exitAgent(Agent a) {//if a agent input /exit in time of a chat method'll remove the agent from map and a client'll be added to array
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (agent == a) {
                if (client != null) {
                    client.sendMessage("chat was ended", "chat");
                    listClients.add(client);
                    client.hasAgent = false;
                }
                mapAgents.remove(agent, client);
                log.info("Agent exited");
                return;
            }
        }
    }

    public synchronized void disconnectClient(Client c) {//if a client input /leave in time of a chat method'll remove the client from map and be added to array
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (client == c) {
                listClients.add(client);
                agent.sendMessage("chat was ended", "chat");
                entry.setValue(null);
                log.info("Client leaved");
                return;
            }
        }
    }

    public synchronized void getInQueue(Client client) { // add client in a queue if he wrote message
        for (int i = 0; i < listClients.size(); i++) {
            if (client == listClients.get(i)) {
                queueClients.add(client);
                listClients.remove(i);
            }
        }
    }
}
