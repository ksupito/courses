package chat.classes;

import chat.server.ServerChat;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Client implements User {
    private static final Logger log = Logger.getLogger(Client.class.getSimpleName());
    private Socket socket;
    private ServerChat server;
    private PrintWriter writer;
    private boolean hasAgent;
    private boolean waitAgent;
    private String name;
    private List<String> listMessages = new LinkedList<>();

    public Client(Socket socket, ServerChat server, String name) {
        this.socket = socket;
        this.server = server;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setHasAgent(boolean hasAgent) {
        this.hasAgent = hasAgent;
    }

    public void start() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             OutputStream out = socket.getOutputStream()) {
            writer = new PrintWriter(out, true);
            hasAgent = false;
            waitAgent = false;
            while (true) {
                String message = reader.readLine();
                if (message != null) {
                    if (message.trim().equals("/exit")) {
                        exit();
                        break;
                    }
                    if (message.trim().equals("/leave")) {
                        leave();
                        continue;
                    }
                    if (!hasAgent && !waitAgent) {
                        server.getInQueue(this);
                        waitAgent = true;
                        checkAgent(message);
                    } else if (!hasAgent && waitAgent) {
                        checkAgent(message);
                    } else if (hasAgent) {
                            server.sendClientMessage(message, this);

                    }
                } else {
                    exit();
                    break;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public synchronized void sendMessage(String message, String name) {
        writer.println(name + " : " + message);
    }

    public synchronized void checkAgent(String message) {
        boolean isChat = server.searchChat();
        if (!isChat) {
            listMessages.add(message);
            sendMessage("There aren't free agents in this moment", server.getNameChat());
        } else if (isChat) {
           if(!checkListMessages()){
               server.sendClientMessage(message, this);
           }
           hasAgent = true;
        }
    }

    private synchronized void exit() throws IOException {
        server.exitClient(this);
        server.searchChat();
        socket.close();
    }

    private synchronized void leave() {
        hasAgent = false;
        waitAgent = false;
        server.disconnectClient(this);
        server.searchChat();
    }

    public synchronized boolean checkListMessages() {
        if (listMessages.size() != 0) {
            for (String message : listMessages) {
                server.sendClientMessage(message, this);
            }
            listMessages.clear();
            return true;
        }
        return false;
    }



}
