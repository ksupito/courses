package chat.classes;

import chat.server.ServerChat;

import java.io.*;
import java.net.Socket;

public class UserThread implements Runnable {
    private Socket socket;
    private ServerChat server;
    BufferedReader reader;
    PrintWriter writer;

    public UserThread(Socket socket, ServerChat server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()){
            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            writer = new PrintWriter(out, true);
            //writer.println(reader.readLine());
            while (true) {
                String registration = reader.readLine();
                if(registration !=null) {
                    if (registration.contains("/agent ")) {
                        String name = registration.replaceFirst("/agent ", "");
                        Agent agent = new Agent(socket, server, name);
                       // try{Thread.sleep(6000);}catch (InterruptedException e){}
                        server.addAgent(agent);
                        agent.start();
                        break;
                    }
                    if (registration.contains("/client ")) {
                        String name = registration.replaceFirst("/client ", "");
                        Client client = new Client(socket, server, name);
                        server.addClient(client);
                        client.start();
                        break;
                    } else {
                        writer.println("Incorrect console command ");
                    }
                }
            }
        } catch (IOException e) {
        }
    }
}
