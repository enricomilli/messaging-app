import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Connection implements Runnable, MessagesCoordinator.MessageListener {
    private final Socket socket;
    private final MessagesCoordinator messagesCoordinator;
    private final PrintWriter writer;
    private final Scanner reader;
    private UserListMap userList;
    private String userId;
    private Integer userPort;
    private String userIp;
    private Boolean isCoordinator = false;
    private Coordinator coordinator;

    public Connection(Socket socket, MessagesCoordinator messagesCoordinator, UserListMap userList)
            throws IOException {
        this.socket = socket;
        this.messagesCoordinator = messagesCoordinator;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new Scanner(socket.getInputStream());
        this.userList = userList;
    }

    public Boolean isCoordinator() {
        return this.isCoordinator;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserIp() {
        return userIp;
    }

    public Integer getUserPort() {
        return userPort;
    }

    public void makeCoordinator() {
        this.isCoordinator = true;
        this.coordinator = new Coordinator(userList, messagesCoordinator);
    }

    public Boolean matchesAddress(String ip, Integer port) {
        return this.userIp.equals(ip) && this.userPort.equals(port);
    }

    public boolean isAlive() {
        return !socket.isClosed();
    }

    private void handleUserMessages(String msg) throws IOException {
        System.out.println("received user message: " + msg);
        String[] msgSplit = msg.split(": ");
        String username = msgSplit[0];
        String userMsg = msgSplit[1];

        if (userMsg.isEmpty()) {
            writer.println("make sure you write something!");
        } else if (userMsg.startsWith("/")) { // User commands
            handleUserCommands(username, userMsg);
        } else { // The message is broadcasted
            messagesCoordinator.addMessage(username + ": " + userMsg);
        }
    }

    private void handleUserCommands(String username, String userMsg) {
        if (userMsg.startsWith("/leave")) {
            try {
                sendMessageWithPrefix("MSG-FROM-SERVER", "[Server] bye, bye!");
                closeConnection();
            } catch (IOException err) {
                System.out.println("error executing leave command: " + err);
            }
        } else if (userMsg.startsWith("/")) {
            // Let the coordinator handle the command
            messagesCoordinator.addCommand(userMsg, userIp, userPort);
        }
    }

    // messages sent from the client directly, meaning automatic / from code without user input
    // should be prefixed with client:
    private void handleClientMessages(String msg) {
        System.out.println("message from client received: " + msg);
        String[] splitMsg = msg.split(":");
        String command = splitMsg[0];
        String value = splitMsg[1];

        // just printing it out as we're not using it
        System.out.println("Received command: " + command + " with values: " + value);
    }

    private void closeConnection() throws IOException {
        userList.removeUser(userId);
        messagesCoordinator.removeListener(this);

        // If the coordinator disconnects, assign a new one
        if (isCoordinator && userList.size() > 0) {
            System.out.println("getting new coordinator");
            messagesCoordinator.findNewCoordinator();
        }

        writer.close();
        reader.close();
        socket.close();
    }

    // When there is a new message added to the list, send it to the client
    public void onNewMessage(String message) {
        sendMessageWithPrefix("MSG-FROM-CHAT", message);
    }

    // If this is the coordinator, this function handles commands from users
    public void onNewCommand(String requestIp, Integer requestPort, String command) {
        coordinator.handleCommand(requestIp, requestPort, command);
    }

    public void sendMessageWithPrefix(String prefix, String msg) {
        writer.println(prefix + msg);
    }

    private String handleNewUser() {
        if (socket.isClosed()) {
            return "socket is closed";
        }

        // Step 1: Check the username
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

        userId = username;

        // Step 2: Get client details
        String ipAddr = socket.getInetAddress().getHostAddress();
        Integer port = socket.getPort();
        this.userPort = port;
        this.userIp = ipAddr;

        // If there are no users, make this connection the coordinator
        if (userList.size() < 1) {
            this.makeCoordinator();
        }

        // Step 3: Add user to list
        userList.addUser(username, ipAddr, port, this.isCoordinator);
        return null;
    }

    public void run() {
        try {
            messagesCoordinator.addListener(this);
            String newUserErr = handleNewUser();
            if (newUserErr != null) {
                writer.println(newUserErr);
                closeConnection();
                return;
            }
            // Confirm with client that the user can join
            writer.println("confirmation");

            messagesCoordinator.addMessage("[Server] " + userId + " has joined the chat.");

            if (isCoordinator) {
                sendMessageWithPrefix("MSG-TO-COORDINATOR", "You are the coordinator");
            } else {
                // tell this connection who the coordinator is
                sendMessageWithPrefix("MSG-FROM-SERVER", messagesCoordinator.getCoordinatorInfo());
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
                    System.out.println("unknown sender: " + messageLine);
                }
            }

            messagesCoordinator.addMessage("[Server] " + userId + " has left the chat.");

            closeConnection();

        } catch (IOException err) {
            System.err.println("error with client connection: " + err);
        }
    }
}

