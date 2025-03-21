import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Connection implements Runnable, MessagesList.MessageListener {
    private final Socket socket;
    private final MessagesList messagesList;
    private final PrintWriter writer;
    private final Scanner reader;
    private UserListMap userList;
    private String currentUser;

    public Connection(Socket socket, MessagesList messagesList, UserListMap userList)
            throws IOException {
        this.socket = socket;
        this.messagesList = messagesList;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new Scanner(socket.getInputStream());
        this.userList = userList;
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
            userList.print();

            System.out.println("checking if username: " + value + " is taken");
            if (exisitingUser == null) {
                writer.println("available");

            } else {
                writer.println("not-available");
            }
        }

    }

    private void closeConnection() throws IOException {
        this.userList.removeUser(currentUser);
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

    private String handleNewUser() {
        System.out.println("handling new user");
        if (socket.isClosed()) {
            return "socket is closed";
        }

        // step 1:
        // check the username
        String usernameCheckMsg = reader.nextLine();
        String[] checkMsgParts = usernameCheckMsg.split(":", 3);
        if (checkMsgParts.length < 3) {
            return "username check in wrong format";
        }

        String username = checkMsgParts[2];
        if (username == null || username.trim().isEmpty()) {
            return "username is empty";
        }

        if (userList.getUser(username) != null) {
            return "username exists";
        }

        currentUser = username;

        // step 2:
        // get client details
        String ipAddr = socket.getInetAddress().getHostAddress();
        Integer port = socket.getPort();

        // check if coordinator
        Boolean isCoordinator = false;
        if (userList.size() < 2) {
            isCoordinator = true;
        }

        // step 3:
        // add to the user list
        userList.addUser(username, ipAddr, port, isCoordinator);

        return null;
    }

    public void run() {
        try {
            messagesList.addListener(this);

            String newUserErr = handleNewUser();
            if (newUserErr != null) {
                writer.println(newUserErr);
                closeConnection();
                return;
            }
            // confirm with client this user can join
            writer.println("available");

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

            closeConnection();

        } catch (IOException err) {
            System.err.println("error with client connection:" + err);
        }
    }

}
