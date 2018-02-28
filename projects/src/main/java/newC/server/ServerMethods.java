package newC.server;

import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ServerMethods {
    public static Map<AgentUser, ClientUser> mapAgents = new HashMap<>();
    public static List<ClientUser> listClients = new LinkedList<>();
    public static BlockingQueue<ClientUser> userQueue = new ArrayBlockingQueue<>(1000);
    public String chatName = "------------->";
    private static final Logger log = Logger.getLogger(ServerMethods.class.getSimpleName());

    public String getChatName() {
        return chatName;
    }

    public Map<AgentUser, ClientUser> getMapAgents() {
        return mapAgents;
    }

    public synchronized void addAgentToMap(AgentUser agent) {
        mapAgents.put(agent, null);
    }

    public synchronized void addClientToQueue(ClientUser client) {
        userQueue.add(client);
    }

    public BlockingQueue<ClientUser> getUserQueue() {
        return userQueue;
    }

    public synchronized void addClient(ClientUser client) {
        listClients.add(client);
    }

    public List<ClientUser> getListClients() {
        return listClients;
    }

    public synchronized void changeQueue(ClientUser client) {
        for (ClientUser user : listClients) {
            if (user.equals(client)) {
                userQueue.add(client);
                listClients.remove(client);
            }
        }
    }

    public synchronized boolean searchChat() throws IOException {
        for (Map.Entry<AgentUser, ClientUser> entry : mapAgents.entrySet()) {
            if (userQueue.size() != 0) {
                AgentUser agent = entry.getKey();
                ClientUser client = entry.getValue();
                if (agent != null && client == null) {
                    ClientUser clientFromQueue = userQueue.remove();
                    clientFromQueue.setAgentUser(agent);
                    agent.setClientUser(clientFromQueue);
                    mapAgents.put(agent, clientFromQueue);
                    send("new chat!", agent.getDos(), chatName);
                    send("new chat!", clientFromQueue.getDos(), chatName);
                    if (clientFromQueue.getMessages().size() != 0) {
                        sendMessages(agent.getDos(), clientFromQueue.getName(), clientFromQueue);
                    }
                    log.info("new chat started");
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void sendMessages(DataOutputStream dos, String name, ClientUser clientUser) throws IOException {
        for (String message : clientUser.getMessages()) {
            dos.writeUTF(name + " : " + message + "\n");
            dos.flush();
        }
        clientUser.getMessages().clear();
    }

    public synchronized void send(String message, DataOutputStream dos, String name) throws IOException {
        dos.writeUTF(name + " : " + message + "\n");
        dos.flush();
    }

    public synchronized boolean exitClient(ClientUser cl) throws IOException {
        for (Map.Entry<AgentUser, ClientUser> entry : mapAgents.entrySet()) {
            AgentUser agent = entry.getKey();
            ClientUser client = entry.getValue();
            if (client == cl) {                                            // && agent != null
                send("client exited", agent.getDos(), chatName);
                agent.setClientUser(null);
                cl.getDos().writeUTF("cancel");
                mapAgents.replace(agent, null);
                log.info("client exited");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean exitClientFromQueue(ClientUser cl) throws IOException {
        if (userQueue.contains(cl)) {
            cl.getDos().writeUTF("cancel");
            if(cl.getMessages().size()!=0){
                cl.getMessages().clear();
            }
            userQueue.remove(cl);
            return true;
        }
        return false;
    }

    public synchronized boolean exitClientFromList(ClientUser cl) throws IOException {
        for (ClientUser client : listClients) {
            if (client == cl) {
                cl.getDos().writeUTF("cancel");
                listClients.remove(client);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean exitAgent(AgentUser ag) throws IOException {
        for (Map.Entry<AgentUser, ClientUser> entry : mapAgents.entrySet()) {
            AgentUser agent = entry.getKey();
            ClientUser client = entry.getValue();
            if (agent == ag) {
                mapAgents.remove(agent);
                if (client != null) {
                    userQueue.add(client);
                    client.setAgentUser(null);
                    send("agent exited", client.getDos(), chatName);
                }
                searchChat();
                agent.getDos().writeUTF("cancel");
                log.info("agent exited");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean leaveClient(ClientUser cl) throws IOException {
        for (Map.Entry<AgentUser, ClientUser> entry : mapAgents.entrySet()) {
            AgentUser agent = entry.getKey();
            ClientUser client = entry.getValue();
            if (client == cl) {     //&& agent != null
                listClients.add(client);
                entry.setValue(null);
                send("client leaved", agent.getDos(), chatName);
                agent.setClientUser(null);
                log.info("client leaved");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean leaveClientFromQueue(ClientUser cl) {
        if (userQueue.contains(cl)) {
            userQueue.remove(cl);
            listClients.add(cl);
            return true;
        }
        return false;
    }
}
