import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;

public class MessagesCoordinatorTest {

    // Dummy implementation of the MessageListener interface for testing.
    class DummyListener implements MessagesCoordinator.MessageListener {
        String id;
        String ip;
        int port;
        boolean alive;
        boolean coordinator;
        String lastMessage = "";
        String lastCommand = "";

        DummyListener(String id, String ip, int port, boolean alive, boolean coordinator) {
            this.id = id;
            this.ip = ip;
            this.port = port;
            this.alive = alive;
            this.coordinator = coordinator;
        }

        @Override
        public void onNewMessage(String message) {
            lastMessage = message;
        }

        @Override
        public void onNewCommand(String requestIp, Integer requestPort, String command) {
            lastCommand = command;
        }

        @Override
        public void makeCoordinator() {
            coordinator = true;
        }

        @Override
        public Boolean matchesAddress(String ip, Integer port) {
            return this.ip.equals(ip) && this.port == port;
        }

        @Override
        public Boolean isCoordinator() {
            return coordinator;
        }

        @Override
        public String getUserId() {
            return id;
        }

        @Override
        public String getUserIp() {
            return ip;
        }

        @Override
        public Integer getUserPort() {
            return port;
        }

        @Override
        public boolean isAlive() {
            return alive;
        }
    }

    @Test
    public void testAddAndRemoveListener() {
        MessagesCoordinator mc = new MessagesCoordinator();
        DummyListener listener1 = new DummyListener("L1", "127.0.0.1", 1000, true, false);
        mc.addListener(listener1);

        List<MessagesCoordinator.MessageListener> listeners = mc.getListeners();
        assertTrue("Listener list should contain listener1", listeners.contains(listener1));

        mc.removeListener(listener1);
        listeners = mc.getListeners();
        assertFalse("Listener list should not contain listener1 after removal", listeners.contains(listener1));
    }

    @Test
    public void testAddMessageNotifiesListener() {
        MessagesCoordinator mc = new MessagesCoordinator();
        DummyListener listener1 = new DummyListener("L1", "127.0.0.1", 1000, true, false);
        mc.addListener(listener1);

        mc.addMessage("Hello World");
        // Expect that the listener’s onNewMessage() was called:
        assertEquals("Hello World", listener1.lastMessage);
    }

    @Test
    public void testGetCoordinatorInfo() {
        MessagesCoordinator mc = new MessagesCoordinator();
        DummyListener listener1 = new DummyListener("L1", "127.0.0.1", 1000, true, false);
        DummyListener listener2 = new DummyListener("L2", "127.0.0.2", 1001, true, true);
        mc.addListener(listener1);
        mc.addListener(listener2);

        String coordinatorInfo = mc.getCoordinatorInfo();
        assertTrue("Coordinator info should mention L2", coordinatorInfo.contains("L2"));
    }

}
