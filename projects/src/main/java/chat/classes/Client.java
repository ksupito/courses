package chat.classes;

import chat.server.ServerChat;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Client implements User {
    private static final Logger log = Logger.getLogger(Reader.class.getSimpleName());
    private Socket socket;
    private ServerChat server;
    private PrintWriter writer;
    public boolean hasAgent;
    private boolean waitAgent;
    public String name;
    List<String> listMessages = new LinkedList<>();

    public Client(Socket socket, ServerChat server, String name) {
        this.socket = socket;
        this.server = server;
        this.name = name;
    }

    public void start() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {
            writer = new PrintWriter(socket.getOutputStream(), true);
            hasAgent = false;
            waitAgent = false;
            while (true) {
                String message = reader.readLine();
                if (message != null) {
                if (message.equals("/exit")) {
                    exit();
                    break;
                }
                if (message.equals("/leave")) {
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
                  // if (message != null) {
                        if (message.equals("/exit")) {
                            exit();
                            break;
                        }
                        if (message.equals("/leave")) {
                            leave();
                            continue;
                        } else {
                            server.sendClientMessage(message, this);
                        }
                    }
                }else {
                    exit();
                    break;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public synchronized void sendMessage(String message, String name) {
        writer.println(name + " : " + message);
    }

    public synchronized void checkAgent(String message) {
        boolean isChat = server.searchChat();
        if (!isChat) {
            listMessages.add(message);
            sendMessage("There aren't free agents in this moment", "chat");
        } else if (isChat) {
            if (listMessages.size() != 0) {
                for (int i = 0; i < listMessages.size(); i++) {
                    server.sendClientMessage(listMessages.get(i), this);
                }
                hasAgent = true;
            } else {
                server.sendClientMessage(message, this);
                hasAgent = true;
            }
        }
    }

    private synchronized void exit() throws IOException {
        server.exitClient(this);
        server.searchChat();
        socket.close();
    }

    private synchronized void leave() throws IOException {
        hasAgent = false;
        waitAgent = false;
        server.disconnectClient(this);
        sendMessage("chat was ended", "chat");
        server.searchChat();
    }

    public synchronized void checkMessage() {
        if (listMessages.size() != 0) {
            for (int i = 0; i < listMessages.size(); i++) {
                server.sendClientMessage(listMessages.get(i), this);
            }
        }
    }
}
