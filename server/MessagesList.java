import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class MessagesList {
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
    private final List<MessageListener> listeners = new CopyOnWriteArrayList<>();

    public interface MessageListener {
        void onNewMessage(String message);

        void onNewCommand(String command);

        Boolean isCoordinator();
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

    public void addCommand(String command, String ip, Integer port) {

        synchronized (messages) {
            // The notify the coordinator of new command
            for (MessageListener listener : listeners) {
                if (listener.isCoordinator()) {
                    listener.onNewCommand(command);
                }
            }
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
}
