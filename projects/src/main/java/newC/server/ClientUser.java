package newC.server;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ClientUser {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String name;
    private String message;
    private ServerMethods serverMethods;
    private boolean waitAgent = false;
    private AgentUser agentUser = null;
    private List<String> messages = new LinkedList<>();

    public ClientUser(DataInputStream dis, DataOutputStream dos, Socket socket, String name) {
        this.name = name;
        this.dis = dis;
        this.dos = dos;
        this.socket = socket;
    }

    public List<String> getMessages() {
        return messages;
    }

    public synchronized void addMessage(String message) {
        messages.add(message);
    }

    public String getName() {
        return name;
    }

    public void setAgentUser(AgentUser agent) {
        this.agentUser = agent;
    }

    public DataOutputStream getDos() {
        return dos;
    }

    public void read() throws IOException {
        try (
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();) {
            dis = new DataInputStream(sin);
            dos = new DataOutputStream(sout);
            serverMethods = new ServerMethods();
            while (!socket.isClosed()) {
                message = dis.readUTF();
                if (message.trim().equals("/exit")) {
                    /*if (agentUser != null) {
                        if (serverMethods.exitClient(this)) {
                            serverMethods.searchChat();
                            socket.close();
                            break;
                        }
                    }
                    if (waitAgent == true) {
                        serverMethods.exitClientFromQueue(this);
                        serverMethods.searchChat();
                        socket.close();
                        break;
                    } else {
                        serverMethods.exitClientFromList(this);
                        serverMethods.searchChat();
                        socket.close();
                        break;
                    }*/
                    exit();
                    socket.close();
                    serverMethods.searchChat();
                    break;

                } else if (message.trim().equals("/leave")) {
                    /*if (agentUser != null) {
                        if (serverMethods.leaveClient(this)) {
                            serverMethods.searchChat();
                            continue;
                        }
                    } else if (waitAgent == true) {
                        if (serverMethods.leaveClientFromQueue(this)) {
                            serverMethods.searchChat();
                            continue;
                        }
                    }else {continue;}*/
                    leave();
                    serverMethods.searchChat();
                    continue;
                }
                if (agentUser == null && !waitAgent) {
                    serverMethods.changeQueue(this);
                    waitAgent = true;
                    /*if (serverMethods.searchChat()) {
                        serverMethods.send(message, agentUser.getDos(), name);
                        continue;
                    } else {
                        this.addMessage(message);
                        serverMethods.send("wait please!", this.getDos(), serverMethods.getChatName());
                        continue;
                    }*/
                    waitChat();
                    continue;
                }
                if (waitAgent && agentUser == null) {
                    /*if (serverMethods.searchChat()) {
                        serverMethods.send(message, agentUser.getDos(), name);
                        continue;
                    } else {
                        this.addMessage(message);
                        serverMethods.send("wait please!", this.getDos(), serverMethods.getChatName());
                        continue;
                    }*/
                    waitChat();
                    continue;
                } else {
                    if (agentUser != null) {
                        serverMethods.send(message, agentUser.getDos(), name);
                    }
                }
            }
        }
    }

    private void leave() throws IOException {
        if (agentUser != null) {
            serverMethods.leaveClient(this);

        } else if (waitAgent) {
            serverMethods.leaveClientFromQueue(this);
        }
    }

    private void exit() throws IOException {
        if (agentUser != null) {
            serverMethods.exitClient(this);
        }
        if (waitAgent && agentUser == null) {
            serverMethods.exitClientFromQueue(this);
        } else {
            serverMethods.exitClientFromList(this);
        }
    }

    private void waitChat() throws IOException {
        if (serverMethods.searchChat()) {
            serverMethods.send(message, agentUser.getDos(), name);
        } else {
            this.addMessage(message);
            serverMethods.send("wait please!", this.getDos(), serverMethods.getChatName());

        }
    }
}
