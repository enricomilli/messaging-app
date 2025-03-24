import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class MessagesCoordinator {
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
    private final List<MessageListener> listeners = new CopyOnWriteArrayList<>();

    public interface MessageListener {
        void onNewMessage(String message);
        void onNewCommand(String requestIp, Integer requestPort, String command);
        void makeCoordinator();
        Boolean matchesAddress(String ip, Integer port);

        Boolean isCoordinator();
        String getUserId();
        String getUserIp();
        Integer getUserPort();
    }

    public void addListener(MessageListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MessageListener listener) {
        listeners.remove(listener);
    }

    // sends the command to the thread handling the coordinator
    public void addCommand(String command, String ip, Integer port) {

        synchronized (messages) {
            for (MessageListener listener : listeners) {
                // The notify the coordinator of new command
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
                // The notify the coordinator of new command
                if (listener.isCoordinator()) {
                    return "Coordinator is: " + listener.getUserId() + " with ip: " + listener.getUserIp() + " and port: " + listener.getUserPort();
                }
            }

            return "Error getting coordinator info";
        }
    }

    public void findNewCoordinator() {

        for (MessageListener listener : listeners) {
            if (listener.isCoordinator()) continue;

            // the first person who isn't a coordinator will become it
            listener.makeCoordinator();
            addMessage("the new coordinator is: " + listener.getUserId() + " with ip: " + listener.getUserIp() + " and port: " + listener.getUserPort());
            return;
        }

        addMessage("could not get new coordinator");
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
                // Find the connection that matches the IP and port
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
                // Find the connection that matches the IP and port
                if (listener instanceof Connection) {
                    Connection connection = (Connection) listener;
                    if (connection.matchesAddress(ip, port)) {
                        connection.sendMessageWithPrefix("MSG-FROM-SERVER", message);
                        break;
                    }
                }
            }
        }
    }

}
