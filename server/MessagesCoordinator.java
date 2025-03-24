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

        Boolean matchesAddress(String ip, Integer port);

        Boolean isCoordinator();
        String getUserId();
        String getUserIp();
        Integer getUserPort();
    }

    public void addListener(MessageListener listener) {
        listeners.add(listener);

        // Uncomment to send all the previous messages to joining clients
        // synchronized (messages) {
        // for (String message : messages) {
        // listener.onNewMessage(message);
        // }
        // }
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
                    System.out.println(listener.toString() + " is the handling coordinator");
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

    public void addMessage(String message) {

        synchronized (messages) {
            messages.add(message);
            // Notify all listeners
            for (MessageListener listener : listeners) {
                listener.onNewMessage(message);
            }
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
