import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import java.util.Scanner;

class Client {
    private static final String MOVE_TO_LINE_START = "\r";
    private static final String CLEAR_LINE = "\033[2K";

    private static void setUpKeymaps(Socket socket, PrintWriter writer, Scanner server) {
        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("\nExiting chat");

                closeConnection(socket, server, writer);
                System.exit(0);
            }
        });
    }

    private static String checkUsernameAvailability(PrintWriter writer, Scanner server, String username) {
        writer.println("client:CHECK-USERNAME:" + username);
        String response = server.nextLine();

        if (response.equals("available")) {
            return null;
        } else {
            return response;
        }

    }

    private static void closeConnection(Socket socket, Scanner server, PrintWriter writer) {
        try {
            socket.close();
        } catch (Exception err) {
            System.out.println("error closing socket: " + socket);
        }
        server.close();
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        FlagHandler flagHandler = new FlagHandler(args);

        ClientConfig client = ClientConfig.build(flagHandler);
        if (client.getErrorMessage() != null) {
            System.out.println("Error occurred: \n" + client.getErrorMessage());
            return;
        }

        System.out.println(
                "Joining server at: " + client.getTargetIp() + ":" + client.getTargetPort() + " as " + client.getId());

        try {
            Socket socket = new Socket(client.getTargetIp(), client.getTargetPort());
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner server = new Scanner(socket.getInputStream());
            Scanner input = new Scanner(System.in);

            // map ctrl-c for controled exit
            setUpKeymaps(socket, writer, server);

            // Use messagePrinter to keep the input in front of all messages
            MessagePrinter messagePrinter = new MessagePrinter(client.getId());

            String usernameErr = checkUsernameAvailability(writer, server, client.getId());
            if (usernameErr != null) {
                System.out.println("error joining: " + usernameErr);
                closeConnection(socket, server, writer);
                input.close();
                return;
            }

            // live messages chat
            MessagesHandler messagesHandler = new MessagesHandler(socket, server, client, messagePrinter);
            Thread messagesHandlerThread = new Thread(messagesHandler);
            messagesHandlerThread.start();

            while (!socket.isClosed()) {
                String message;
                System.out.print(client.getId() + ": ");

                // waits here till user presses enter
                message = input.nextLine();
                if (message != null && !message.trim().isEmpty()) {
                    // Clear the input line
                    System.out.print(MOVE_TO_LINE_START + CLEAR_LINE);

                    // send message to the server
                    writer.println("user:" + client.getId() + ": " + message);
                } else {
                    messagePrinter.printMessage("make sure you write something!");
                    System.out.print(MOVE_TO_LINE_START + CLEAR_LINE);
                }

            }

            closeConnection(socket, server, writer);
            input.close();
        } catch (Error err) {
            System.out.println("error running client: " + err);
        }
    }
}
