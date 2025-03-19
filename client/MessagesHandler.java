
// This class will accepted the writer and constantly print out the messages received from the server
// This is so that we dont have to wait for the user input to refresh the messages

import java.net.Socket;
import java.util.Scanner;

class MessagesHandler implements Runnable {
    private final Scanner server;
    private final ClientConfig client;
    private final Socket socket;
    private final MessagePrinter messagePrinter;
    // ANSI escape sequences for moving the cursor and clearing lines.
    // private static final String ANSI_UP_ONE = "\033[A";
    // private static final String ANSI_CLEAR_LINE = "\033[K";

    public MessagesHandler(Socket socket, Scanner server, ClientConfig client, MessagePrinter messagePrinter) {
        this.server = server;
        this.client = client;
        this.socket = socket;
        this.messagePrinter = messagePrinter;
    }

    private void handleServerMessage(String msg, String clientId) {

        if (msg.startsWith("MSG-FROM-CHAT")) { // server sends down new messages from the chat with this prefix
            String newUserMsg = msg.replaceFirst("MSG-FROM-CHAT", "");

            if (newUserMsg.startsWith(client.getId() + ":")) {
                newUserMsg = newUserMsg.replaceFirst(client.getId() + ": ", "you: ");
                return;
            }

            this.messagePrinter.printMessage(newUserMsg);

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
