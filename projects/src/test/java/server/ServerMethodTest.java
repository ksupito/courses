package server;

import newC.server.AgentUser;
import newC.server.ClientUser;
import newC.server.ServerMethods;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.*;

public class ServerMethodTest {
    Socket socket = Mockito.mock(Socket.class);
    DataOutputStream out = Mockito.mock(DataOutputStream.class);
    DataInputStream in = Mockito.mock(DataInputStream.class);
    AgentUser agentUser = new AgentUser(in, out, socket, "nn");
    ClientUser clientUser = new ClientUser(in, out, socket, "nn");
    ServerMethods serverMethods = new ServerMethods();

    @Test
    public void searchChatTest() throws IOException {
        serverMethods.addClientToQueue(clientUser);
        serverMethods.addAgentToMap(agentUser);
        assertEquals(1, serverMethods.getUserQueue().size());
        assertEquals(1, serverMethods.getMapAgents().size());
        assertTrue(serverMethods.searchChat());
        assertEquals(0, serverMethods.getUserQueue().size());
        serverMethods.getMapAgents().clear();
    }

    @Test
    public void searchChatTestFalse() throws IOException {
        serverMethods.addAgentToMap(agentUser);
        assertEquals(0, serverMethods.getUserQueue().size());
        assertEquals(1, serverMethods.getMapAgents().size());
        assertFalse(serverMethods.searchChat());
        serverMethods.getMapAgents().clear();
    }

    @Test
    public void searchChatTestFalseWithTwoClient() throws IOException {
        serverMethods.addClientToQueue(clientUser);
        serverMethods.getMapAgents().put(agentUser, new ClientUser(in, out, socket, "11"));
        assertEquals(1, serverMethods.getUserQueue().size());
        assertEquals(1, serverMethods.getMapAgents().size());
        assertFalse(serverMethods.searchChat());
        assertEquals(1, serverMethods.getUserQueue().size());
        serverMethods.getMapAgents().clear();
        serverMethods.getUserQueue().clear();
    }

    @Test
    public void exitClientTest() throws IOException {
        serverMethods.getMapAgents().put(agentUser, clientUser);
        assertTrue(serverMethods.exitClient(clientUser));
        ClientUser client = serverMethods.getMapAgents().get(agentUser);
        assertEquals(null, client);
        serverMethods.getMapAgents().clear();
    }

    @Test
    public void exitClientTestFalse() throws IOException {
        serverMethods.getMapAgents().put(agentUser, clientUser);
        assertFalse(serverMethods.exitClient(new ClientUser(in, out, socket, "11")));
        ClientUser client = serverMethods.getMapAgents().get(agentUser);
        assertEquals(clientUser, client);
        serverMethods.getMapAgents().clear();
    }

    @Test
    public void exitClientFromQueueTest() throws IOException {
        serverMethods.getUserQueue().add(clientUser);
        assertTrue(serverMethods.exitClientFromQueue(clientUser));
        assertEquals(0, serverMethods.getUserQueue().size());
    }

    @Test
    public void exitClientFromQueueTestFalse() throws IOException {
        serverMethods.getUserQueue().add(clientUser);
        assertFalse(serverMethods.exitClientFromQueue(new ClientUser(in, out, socket, "11")));
        assertEquals(1, serverMethods.getUserQueue().size());
        serverMethods.getUserQueue().clear();
    }

    @Test
    public void exitClientFromListTest() throws IOException {
        serverMethods.addClient(clientUser);
        assertTrue(serverMethods.exitClientFromList(clientUser));
        assertEquals(0, serverMethods.getListClients().size());
    }

    @Test
    public void exitClientFromListTestFalse() throws IOException {
        serverMethods.addClient(clientUser);
        assertFalse(serverMethods.exitClientFromList(new ClientUser(in, out, socket, "11")));
        assertEquals(1, serverMethods.getListClients().size());
        serverMethods.getListClients().clear();
    }

    @Test
    public void exitAgentTest() throws IOException {
        serverMethods.addAgentToMap(agentUser);
        assertTrue(serverMethods.exitAgent(agentUser));
        assertEquals(0, serverMethods.getMapAgents().size());
    }

    @Test
    public void exitAgentWithClientTest() throws IOException {
        serverMethods.getMapAgents().put(agentUser,clientUser);
        assertEquals(0,serverMethods.getUserQueue().size());
        assertTrue(serverMethods.exitAgent(agentUser));
        assertEquals(1,serverMethods.getUserQueue().size());
        assertEquals(0, serverMethods.getMapAgents().size());
        serverMethods.getUserQueue().clear();
    }

    @Test
    public void exitAgentTestFalse() throws IOException {
        serverMethods.addAgentToMap(agentUser);
        assertFalse(serverMethods.exitAgent(new AgentUser(in, out, socket, "11")));
        assertEquals(1, serverMethods.getMapAgents().size());
        serverMethods.getMapAgents().clear();
    }

    @Test
    public void lieveClientFromQueueTest() throws IOException {
        serverMethods.getUserQueue().add(clientUser);
        assertEquals(0, serverMethods.getListClients().size());
        assertTrue(serverMethods.leaveClientFromQueue(clientUser));
        assertEquals(0, serverMethods.getUserQueue().size());
        assertEquals(1, serverMethods.getListClients().size());
        serverMethods.getListClients().clear();

    }

    @Test
    public void lieveClientFromQueueTestFalse() throws IOException {
        serverMethods.getUserQueue().add(clientUser);
        assertEquals(0, serverMethods.getListClients().size());
        assertFalse(serverMethods.leaveClientFromQueue(new ClientUser(in, out, socket, "11")));
        assertEquals(1, serverMethods.getUserQueue().size());
        assertEquals(0, serverMethods.getListClients().size());
        serverMethods.getUserQueue().clear();
    }

    @Test
    public void lieveClientTest() throws IOException {
        serverMethods.getMapAgents().put(agentUser, clientUser);
        assertEquals(0, serverMethods.getListClients().size());
        assertEquals(1, serverMethods.getMapAgents().size());
        assertTrue(serverMethods.leaveClient(clientUser));
        ClientUser client = serverMethods.getMapAgents().get(agentUser);
        assertEquals(null, client);
        assertEquals(1, serverMethods.getListClients().size());
        serverMethods.getMapAgents().clear();
        serverMethods.getListClients().clear();

    }

    @Test
    public void lieveClientTestFalse() throws IOException {
        serverMethods.getMapAgents().put(agentUser, clientUser);
        assertEquals(0, serverMethods.getListClients().size());
        assertEquals(1, serverMethods.getMapAgents().size());
        assertFalse(serverMethods.leaveClient(new ClientUser(in, out, socket, "11")));
        ClientUser client = serverMethods.getMapAgents().get(agentUser);
        assertEquals(clientUser, client);
        assertEquals(0, serverMethods.getListClients().size());
        serverMethods.getMapAgents().clear();
    }

}
