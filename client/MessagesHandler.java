import java.net.Socket;
import java.util.Scanner;

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

        if (msg.startsWith("MSG-FROM-CHAT")) { // server sends down new messages from the chat with this prefix

            String newUserMsg = msg.replaceFirst("MSG-FROM-CHAT", "");
            if (newUserMsg.startsWith(client.getId() + ":")) {
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
