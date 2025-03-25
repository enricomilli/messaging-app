import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class MessagesCoordinator {
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
    private final List<MessageListener> listeners = new CopyOnWriteArrayList<>();

    // Modified MessageListener interface (implements observer pattern)
    public interface MessageListener {
        void onNewMessage(String message);
        void onNewCommand(String requestIp, Integer requestPort, String command);
        void makeCoordinator();
        Boolean matchesAddress(String ip, Integer port);
        Boolean isCoordinator();
        String getUserId();
        String getUserIp();
        Integer getUserPort();
        boolean isAlive();
    }

    public void addListener(MessageListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MessageListener listener) {
        listeners.remove(listener);
    }

    // get listeners for use in heartbeat checking.
    public List<MessageListener> getListeners() {
        synchronized (messages) {
            return new ArrayList<>(listeners);
        }
    }

    // Sends the command to the thread handling the coordinator
    public void addCommand(String command, String ip, Integer port) {
        synchronized (messages) {
            for (MessageListener listener : listeners) {
                // Notify the coordinator of a new command
                if (listener.isCoordinator()) {
                    System.out.println(listener.getUserId() + " is the coordinator handling new command");
                    listener.onNewCommand(ip, port, command);
                }
            }
        }
    }

    public String getCoordinatorInfo() {
        synchronized (messages) {
            for (MessageListener listener : listeners) {
                if (listener.isCoordinator()) {
                    return "Coordinator is: " + listener.getUserId() +
                           " with ip: " + listener.getUserIp() +
                           " and port: " + listener.getUserPort();
                }
            }
            return "Error getting coordinator info";
        }
    }

    // Finds a new coordinator if the current one disconnects
    public void findNewCoordinator() {
        for (MessageListener listener : listeners) {
            if (listener.isCoordinator()) continue;
            listener.makeCoordinator();
            addMessage("The new coordinator is: " + listener.getUserId() +
                       " with ip: " + listener.getUserIp() +
                       " and port: " + listener.getUserPort());
            return;
        }
        addMessage("Could not get new coordinator");
    }

    public void addMessage(String message) {
        synchronized (messages) {
            messages.add(message);
            // Notify all listeners
            for (MessageListener listener : listeners) {
                listener.onNewMessage(message);
            }
        }
    }

    public String getUsernameByAddress(String ip, Integer port) {
        synchronized (messages) {
            for (MessageListener listener : listeners) {
                if (listener instanceof Connection) {
                    Connection connection = (Connection) listener;
                    if (connection.matchesAddress(ip, port)) {
                        return connection.getUserId();
                    }
                }
            }
            return "username not found";
        }
    }

    public void sendMsgToIp(String ip, Integer port, String message) {
        synchronized (messages) {
            for (MessageListener listener : listeners) {
                if (listener instanceof Connection) {
                    Connection connection = (Connection) listener;
                    if (connection.matchesAddress(ip, port)) {
                        connection.sendMessageWithPrefix("MSG-FROM-COORDINATOR", message);
                        break;
                    }
                }
            }
        }
    }
}

