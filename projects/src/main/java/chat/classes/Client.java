package chat.classes;

import chat.server.ServerChat;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable, User {
    private static final Logger log = Logger.getLogger(Reader.class.getSimpleName());
    private Socket socket;
    private ServerChat server;
    private PrintWriter writer;
    public boolean hasAgent;
    private boolean waitAgent;
    public String name;
    private String message;

    public Client(Socket socket, ServerChat server, String name) {
        this.socket = socket;
        this.server = server;
        this.name = name;
    }

    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {
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
                    if (message != null) {
                        if (message.equals("/exit")) {
                            exit();
                            break;
                        }
                        if (message.equals("/leave")) {
                            leave();
                        } else {
                            server.sendClientMessage(message, this);
                        }
                    } else {
                        exit();
                        break;
                    }
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
            sendMessage("There aren't free agents in this moment", "chat");
            // this.message = message;
        } else if (isChat) {
            server.sendClientMessage(message, this);
            hasAgent = true;
        }
    }
    private synchronized void exit() throws IOException{
        server.exitClient(this);
        server.searchChat();
        socket.close();
    }
    private synchronized void leave() throws IOException{
        hasAgent = false;
        waitAgent = false;
        server.disconnectClient(this);
        sendMessage("chat was ended", "chat");
        server.searchChat();
    }
}
