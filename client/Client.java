import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class Client {
    private static final String MOVE_TO_LINE_START = "\r";
    private static final String CLEAR_LINE = "\033[2K";

    // Handling controlc-c
    private static void registerShutdownHook(Socket socket, PrintWriter writer, Scanner server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nExiting chat...");
            closeConnection(socket, server, writer);
        }));
    }

    private static String checkUsernameAvailability(PrintWriter writer, Scanner server, MessagePrinter messagePrinter,
            String username) {
        writer.println("client:CHECK-USERNAME:" + username);
        String response = server.nextLine();

        if (response.equals("confirmation")) {
            messagePrinter.printMessage("Successfully joined!");
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

        MessagePrinter messagePrinter = new MessagePrinter(client.getId());
        messagePrinter.printMessage("Joining server at: " + client.getTargetIp() + ":" + client.getTargetPort()
                + " as " + client.getId());

        final Socket socket = new Socket(client.getTargetIp(), client.getTargetPort());
        final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        final Scanner server = new Scanner(socket.getInputStream());
        final Scanner input = new Scanner(System.in);

        // This function runs when user pressed Control-C
        registerShutdownHook(socket, writer, server);

        try {
            String usernameErr = checkUsernameAvailability(writer, server, messagePrinter, client.getId());
            if (usernameErr != null) {
                System.out.println("error joining: " + usernameErr);
                closeConnection(socket, server, writer);
                input.close();
                return;
            }

            // Start the live chat messaging handler in its own thread.
            MessagesHandler messagesHandler = new MessagesHandler(socket, server, client, messagePrinter);
            Thread messagesHandlerThread = new Thread(messagesHandler);
            messagesHandlerThread.start();

            String instructionsMsg = "Available Commands:\n/leave - leaves the chat\n/message targetUser 'your message' "
                    + "to private message someone\n/list or /members - to list all the members in the chat";
            messagePrinter.printMessage(instructionsMsg);

            while (!socket.isClosed()) {
                messagePrinter.printMessage("\033[A"); // Move cursor up to keep input in front.
                String message = input.nextLine();
                if (message != null && !message.trim().isEmpty()) {
                    System.out.print(MOVE_TO_LINE_START + CLEAR_LINE);
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
