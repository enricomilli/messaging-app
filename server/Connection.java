import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Connection implements Runnable, MessagesList.MessageListener {
    private final Socket socket;
    private final MessagesList messagesList;
    private final PrintWriter writer;
    private final Scanner reader;

    public Connection(Socket socket, MessagesList messagesList) throws IOException {
        this.socket = socket;
        this.messagesList = messagesList;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new Scanner(socket.getInputStream());
    }

    private static void handleCommands(Socket sock, Scanner reader, PrintWriter writer, String command)
            throws IOException {

        if (command.equals("/leave")) {
            System.out.println("client disconnected");
            writer.flush();

            writer.close();
            reader.close();
            sock.close();
        } else {
            writer.println("that command is not recognized");
        }

    }

    // when there is a new message added to the list send it to the client
    public void onNewMessage(String message) {
        this.writer.println("MSG-FROM-CHAT" + message);
    }

    public void run() {
        try {
            messagesList.addListener(this);
            writer.println("connected! send a message anytime.");

            while (!this.socket.isClosed() && this.reader.hasNextLine()) {

                String messageLine = reader.nextLine().trim();
                String[] splitMessage = messageLine.split(": ", 2);
                String username = splitMessage[0];
                String userMessage = splitMessage[1].trim();

                if (userMessage.isEmpty()) {
                    writer.println("make sure you write something!");

                } else if (userMessage.startsWith("/")) {
                    handleCommands(this.socket, reader, writer, userMessage);

                } else {

                    System.out.println("Message received from:" + username);
                    messagesList.addMessage(username + ": " + userMessage);

                }

            }
        } catch (IOException err) {
            System.err.println("error running client:" + err);

        }
    }

}
