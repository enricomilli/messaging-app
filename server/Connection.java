import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Connection implements Runnable, MessagesList.MessageListener {
    private final Socket socket;
    private final MessagesList messagesList;
    private final PrintWriter writer;
    private final Scanner reader;
    private AtomicInteger numOfConnections;
    private UserListMap userList;

    public Connection(Socket socket, MessagesList messagesList, AtomicInteger numOfConnections, UserListMap userList)
            throws IOException {
        this.socket = socket;
        this.messagesList = messagesList;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new Scanner(socket.getInputStream());
        this.userList = userList;
        this.numOfConnections = numOfConnections;
        this.numOfConnections.incrementAndGet();
    }

    private void handleUserMessages(String msg)
            throws IOException {

        System.out.println("received user message: " + msg);
        String[] msgSplit = msg.split(": ");
        String username = msgSplit[0];
        String userMsg = msgSplit[1];

        if (userMsg.isEmpty()) {
            writer.println("make sure you write something!");

        } else if (userMsg.startsWith("/")) { // User commands
            handleUserCommands(username, userMsg);

        } else { // the message is broadcasted

            // messagesList automatically adds MSG-FROM-CHAT prefix
            messagesList.addMessage(username + ": " + userMsg);
        }

    }

    private void handleUserCommands(String username, String userMsg) {

        if (userMsg.startsWith("/leave")) {
            try {
                sendMessageWithPrefix("MSG-FROM-SERVER", "bye, bye!");
                closeConnection();
            } catch (IOException err) {
                System.out.println("error executing leave command" + err);
            }
        }

    }

    private void handleClientMessages(String msg) {
        System.out.println("message from client received: " + msg);
        String[] splitMsg = msg.split(":");
        String command = splitMsg[0];
        String value = splitMsg[1];

        if (command.equals("CHECK-USERNAME")) {
            UserListMap.UserInfo exisitingUser = userList.getUser(value);
            if (exisitingUser == null) {
                writer.println("available");

            } else {
                writer.println("not-available");
            }
        }

    }

    private void closeConnection() throws IOException {
        this.numOfConnections.decrementAndGet();
        this.writer.close();
        this.reader.close();
        this.socket.close();
    }

    // when there is a new message added to the list send it to the client
    public void onNewMessage(String message) {
        sendMessageWithPrefix("MSG-FROM-CHAT", message);
    }

    public void sendMessageWithPrefix(String prefix, String msg) {
        writer.println(prefix + msg);
    }

    public void run() {
        try {
            messagesList.addListener(this);
            writer.println("connected! send a message anytime.");

            if (this.numOfConnections.get() == 1) {
                sendMessageWithPrefix("MSG-TO-COORDINATOR", "your are the coordinator");
            }

            while (!socket.isClosed() && reader.hasNext()) {

                String messageLine = reader.nextLine().trim();

                String[] splitMessage = messageLine.split(":", 2);
                String sender = splitMessage[0];

                if (sender.equals("user")) {
                    handleUserMessages(splitMessage[1]);

                } else if (sender.equals("client")) {
                    handleClientMessages(splitMessage[1]);

                } else {
                    System.out.println("unknown sender:" + messageLine);
                }

            }

        } catch (IOException err) {
            System.err.println("error with client connection:" + err);
        }
    }

}
