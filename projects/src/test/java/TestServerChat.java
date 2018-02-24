import chat.classes.Agent;
import chat.classes.Client;
import chat.server.ServerChat;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class TestServerChat {

    ServerChat serverChat = new ServerChat();
    Client client = Mockito.mock(Client.class);
    Agent agent = Mockito.mock(Agent.class);


    @Test
    public void testExitClientTrue() {
        serverChat.getMapAgents().put(agent, client);
        assertTrue(serverChat.exitClient(client));
    }

    @Test
    public void testExitClientFalse() {
        assertFalse(serverChat.exitClient(client));
    }

    @Test
    public void testExitClientDifferentClient() {
        serverChat.getMapAgents().put(agent, client);
        Client newClient = Mockito.mock(Client.class);
        assertFalse(serverChat.exitClient(newClient));
    }

    @Test
    public void testExitAgentTrue() {
        serverChat.getMapAgents().put(agent, client);
        assertTrue(serverChat.exitAgent(agent));
        assertEquals(0, serverChat.getMapAgents().size());
    }

    @Test
    public void testExitAgentFalse() {
        assertFalse(serverChat.exitAgent(agent));
    }

    @Test
    public void testExitAgentWithOutClient() {
        serverChat.addAgent(agent);
        assertTrue(serverChat.exitAgent(agent));
        assertEquals(0, serverChat.getMapAgents().size());
    }

    @Test
    public void testDisconnectClientTrue() {
        serverChat.getMapAgents().put(agent, client);
        assertTrue(serverChat.disconnectClient(client));
        assertEquals(1, serverChat.getListClients().size());
    }

    @Test
    public void testDisconnectClientFalse() {
        assertFalse(serverChat.disconnectClient(client));
    }

    @Test
    public void testDisconnectClientDifferentClient() {
        serverChat.getMapAgents().put(agent, client);
        Client newClient = Mockito.mock(Client.class);
        assertFalse(serverChat.disconnectClient(newClient));
        assertEquals(0, serverChat.getListClients().size());
    }

    @Test
    public void testSendClientMessageTrue() {
        serverChat.getMapAgents().put(agent, client);
        assertTrue(serverChat.sendClientMessage("message", client));
    }

    @Test
    public void testSendClientMessageFalse() {
        assertFalse(serverChat.sendClientMessage("message", client));
    }
    @Test
    public void testSendClientMessageWithOutAgent() {
        serverChat.getMapAgents().put(null, client);
        assertFalse(serverChat.sendClientMessage("message", client));
    }

    @Test
    public void testSendClientDifferentClient() {
        serverChat.getMapAgents().put(agent, client);
        Client newClient = Mockito.mock(Client.class);
        assertFalse(serverChat.sendClientMessage("message", newClient));
    }

    @Test
    public void testSendAgentMessageTrue() {
        serverChat.getMapAgents().put(agent, client);
        assertTrue(serverChat.sendAgentMessage("message", agent));
    }

    @Test
    public void testSendAgentMessageFalse() {
        serverChat.addAgent(agent);
        assertFalse(serverChat.sendAgentMessage("message", agent));
    }

    @Test
    public void testSearchChatTrue() {
        serverChat.addAgent(agent);
        serverChat.getQueueClients().add(client);
        assertTrue(serverChat.searchChat());
    }

    @Test
    public void testSearchChatClientNotNull() {
        serverChat.getMapAgents().put(agent, client);
        serverChat.getQueueClients().add(client);
        assertFalse(serverChat.searchChat());
    }

    @Test
    public void testSearchChatFalseEmptyQueue() {
        serverChat.addAgent(agent);
        assertFalse(serverChat.searchChat());
    }

    @Test
    public void testSearchChatFalse() {
        serverChat.addAgent(null);
        assertFalse(serverChat.searchChat());
    }

}
