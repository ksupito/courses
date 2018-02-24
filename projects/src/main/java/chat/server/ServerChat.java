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
    private static final Logger log = Logger.getLogger(ServerChat.class.getSimpleName());
    private static final int PORT = 8887;
    private Map<Agent, Client> mapAgents = new HashMap<>();
    private Queue<Client> queueClients = new LinkedList<>();
    private List<Client> listClients = new LinkedList<>();
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    public String nameChat = "------";

    public String getNameChat() {
        return nameChat;
    }

    public Map<Agent, Client> getMapAgents() {
        return mapAgents;
    }

    public List<Client> getListClients() {
        return listClients;
    }

    public Queue<Client> getQueueClients() {
        return queueClients;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                socket = serverSocket.accept();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("Start");
                UserThread userThread = new UserThread(socket, this);
                new Thread(userThread).start();
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
                    Client clientFromQueue = queueClients.remove();
                    clientFromQueue.setHasAgent(true);
                    mapAgents.put(agent, clientFromQueue);
                    agent.sendMessage("new chat!", nameChat);
                    clientFromQueue.sendMessage("new chat!", nameChat);
                    clientFromQueue.checkListMessages();
                    log.info("New chat was started");
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean sendAgentMessage(String message, Agent ag) { //method send message to client
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (agent == ag && client != null) {
                client.sendMessage(message, agent.getName());
                log.info("Message to a client");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean sendClientMessage(String message, Client cl) { //method send message to agent
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (client ==cl  && agent!=null) {
                agent.sendMessage(message, client.getName());
                log.info("Message to an agent");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean exitClient(Client cl) { //if a client input /exit in time of a chat method'll remove the client from map
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (client ==cl  && agent!=null) {
                agent.sendMessage("client exited", nameChat);
                mapAgents.replace(agent, null);
                log.info("Client exited");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean exitAgent(Agent ag) {//if a agent input /exit in time of a chat method'll remove the agent from map and a client'll be added to array
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (agent == ag) {
                if (client != null) {
                    client.sendMessage("agent exited", nameChat);
                    listClients.add(client);
                    client.setHasAgent(false);
                }
                mapAgents.remove(agent);
                log.info("Agent exited");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean disconnectClient(Client cl) {//if a client input /leave in time of a chat method'll remove the client from map and be added to array
        for (Map.Entry<Agent, Client> entry : mapAgents.entrySet()) {
            Agent agent = entry.getKey();
            Client client = entry.getValue();
            if (client ==cl  && agent!=null) {
                listClients.add(client);
                agent.sendMessage("client leaved", nameChat);
                entry.setValue(null);
                log.info("Client leaved");
                return true;
            }
        }
        return false;
    }

    public synchronized void getInQueue(Client client) { // add client in a queue if he wrote message
        for (int i = 0; i < listClients.size(); i++) {
            if (client == listClients.get(i)) {
                queueClients.add(client);
                listClients.remove(i);
            }
        }
    }
    public static void main(String[] args) {
        org.apache.log4j.PropertyConfigurator.configure("src/main/resources/log4j.properties");
        ServerChat server = new ServerChat();
        try {
            server.start();
        } catch (IOException e) {

            log.error(e.getMessage());
        }

    }
}
