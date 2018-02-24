
import chat.classes.Agent;
import chat.classes.Client;
import chat.server.ServerChat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertTrue;

public class TestServerAvailability {

    ServerChat server = Mockito.mock(ServerChat.class);

    ServerSocket serverSocket = Mockito.mock(ServerSocket.class);
    Client client = Mockito.mock(Client.class);
    Agent agent = Mockito.mock(Agent.class);
    Socket socket = Mockito.mock(Socket.class);


    

    @Test
    public void checkServerFalse1() throws IOException {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 8887)) {
        }

    }

   


}
