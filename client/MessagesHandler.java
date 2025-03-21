import java.net.Socket;
import java.util.Scanner;

/**
 * MessagesHandler contains the logic for printing out
 * messages received from the server
 */
class MessagesHandler implements Runnable {
    private final Scanner server;
    private final ClientConfig client;
    private final Socket socket;
    private final MessagePrinter messagePrinter;

    public MessagesHandler(Socket socket, Scanner server, ClientConfig client, MessagePrinter messagePrinter) {
        this.server = server;
        this.client = client;
        this.socket = socket;
        this.messagePrinter = messagePrinter;
    }

    // prefixes sent by the server are handled here
    private void handleServerMessage(String msg, String clientId) {

        if (msg.startsWith("MSG-FROM-CHAT")) { // server sends down new chats with this prefix
            String newUserMsg = msg.replaceFirst("MSG-FROM-CHAT", "");
            if (newUserMsg.startsWith(client.getId() + ":")) {
                return;
            }

            this.messagePrinter.printMessage(newUserMsg);

        } else if (msg.startsWith("MSG-FROM-USER")) {
            // private message from a user goes here
            this.messagePrinter.printMessage("Private messaged received from username: message");

        } else if (msg.startsWith("MSG-FROM-COORDINATOR")) {
            // messages from the coordinator

        } else if (msg.startsWith("MSG-TO-COORDINATOR")) {
            // messages only to the coordinator

        } else if (msg.startsWith("MSG-FROM-SERVER")) {
            // messages from the server
            String serverMsg = msg.replaceFirst("MSG-FROM-SERVER", "");
            this.messagePrinter.printMessage(serverMsg);

        } else {
            this.messagePrinter.printMessage(msg);
        }
    }

    public void run() {
        while (!this.socket.isClosed()) {

            // wait for response
            if (this.server.hasNextLine()) {
                String serverRes = server.nextLine();
                handleServerMessage(serverRes, this.client.getId());

            } else {
                System.out.println("Lost connection to server");
                break;

            }

        }

    }
}
