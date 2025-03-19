import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Connection implements Runnable {
    private Socket socket;

    public Connection(Socket socket) {
        this.socket = socket;
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

    public void run() {
        try (
                Socket sock = this.socket; // This will close the socket when done
                PrintWriter writer = new PrintWriter(sock.getOutputStream(), true);
                Scanner reader = new Scanner(sock.getInputStream())) {

            writer.println("connected! send a message anytime.");

            while (!sock.isClosed() && reader.hasNextLine()) {

                String messageLine = reader.nextLine().trim();
                String[] splitMessage = messageLine.split(":", 2);
                String username = splitMessage[0];
                String userMessage = splitMessage[1].trim();

                if (userMessage.isEmpty()) {
                    writer.println("make sure you write something!");
                } else if (userMessage.startsWith("/")) {
                    handleCommands(sock, reader, writer, userMessage);
                } else {
                    writer.println("MSG-CONFIRMED"); // Send confirmation back to client

                    System.out.println("Message from:" + username);
                    System.out.println("Message content: " + userMessage);
                }

            }
        } catch (IOException err) {
            System.err.println("error running client:" + err);

        }
    }

}
