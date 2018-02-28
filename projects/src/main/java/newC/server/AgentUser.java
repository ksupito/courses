package newC.server;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class AgentUser {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String name;
    private String message;
    private ServerMethods serverMethods;
    private ClientUser clientUser = null;
    private static final Logger log = Logger.getLogger(AgentUser.class.getSimpleName());

    public AgentUser(DataInputStream dis, DataOutputStream dos, Socket socket, String name) {
        this.name = name;
        this.dis = dis;
        this.dos = dos;
        this.socket = socket;
    }

    public void setClientUser(ClientUser clientUser) {
        this.clientUser = clientUser;
    }

    public DataOutputStream getDos() {
        return dos;
    }

    public void read() throws IOException {
        try (InputStream sin = socket.getInputStream();
             OutputStream sout = socket.getOutputStream();) {
            dis = new DataInputStream(sin);
            dos = new DataOutputStream(sout);
            serverMethods = new ServerMethods();
            serverMethods.searchChat();
            while (!socket.isClosed()) {
                message = dis.readUTF();
                if (message.trim().equals("/exit")) {
                    if (serverMethods.exitAgent(this)) {
                        socket.close();
                        break;
                    }
                }
                if (clientUser != null) {
                    serverMethods.send(message, clientUser.getDos(), name);
                }
            }
        }
    }
}
