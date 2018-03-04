package newC.server;

//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private ServerMethods serverMethods;
    private String name;
    private String registration;
    AgentUser agent = null;
    ClientUser client = null;
    private static final Logger log = Logger.getLogger(ServerThread.class.getSimpleName());

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream sin = socket.getInputStream();
             OutputStream sout = socket.getOutputStream();) {
            dis = new DataInputStream(sin);
            dos = new DataOutputStream(sout);
            serverMethods = new ServerMethods();
            registration = dis.readUTF();
            if (registration != null) {
                if (registration.contains("/a")) {
                    createAgent();
                    return;
                }
                if (registration.contains("/c")) {
                    createClient();
                    return;
                }
            }
        } catch (IOException e) {
            criticalExit();
            log.error(e.getMessage());
        }
    }

    private void createAgent() throws IOException {
        name = registration.replaceFirst("/a ", "");
        agent = new AgentUser(dis, dos, socket, name);
        serverMethods.addAgentToMap(agent);
        log.info("agent registered");
        agent.read();
    }

    private void createClient() throws IOException {
        name = registration.replaceFirst("/c ", "");
        client = new ClientUser(dis, dos, socket, name);
        serverMethods.addClient(client);

        serverMethods.send("type message, pleaes!", dos, serverMethods.getChatName());
        log.info("client registered");
        client.read();
    }

    private void criticalExit() {
        try {
            if (agent != null) {
                serverMethods.exitAgent(agent);
            } else if (client != null) {
                client.getExit();
            }

            socket.close();
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }
}
