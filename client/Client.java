import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class Client {
    private static final String MOVE_TO_LINE_START = "\r";
    private static final String CLEAR_LINE = "\033[2K";

    private static String readConsoleInput(Console console, String userId, InputBuffer inputBuffer) {
        StringBuilder input = new StringBuilder();
        System.out.print(userId + ": ");

        while (true) {
            try {
                char c = (char) System.in.read();

                if (c == '\n' || c == '\r') {
                    String result = input.toString();
                    input.setLength(0);
                    return result;
                } else if (c == 127 || c == 8) { // Backspace
                    if (input.length() > 0) {
                        input.deleteCharAt(input.length() - 1);
                        System.out.print("\b \b"); // Move back, print space, move back again
                    }
                } else {
                    input.append(c);
                    System.out.print(c);
                }

                // Update the input buffer
                inputBuffer.setCurrentInput(input.toString());

            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
                return null;
            }
        }
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

        try (Socket socket = new Socket(client.getTargetIp(), client.getTargetPort())) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner server = new Scanner(socket.getInputStream());
            Console console = System.console();

            // If console is not available, fall back to Scanner
            Scanner input = (console == null) ? new Scanner(System.in) : null;

            // Create a shared buffer to store current input
            InputBuffer inputBuffer = new InputBuffer();

            // Create message printer with input buffer
            MessagePrinter messagePrinter = new MessagePrinter(client.getId(), inputBuffer);

            // live messages chat
            Thread messagesHandler = new Thread(new MessagesHandler(socket, server, client, messagePrinter));
            messagesHandler.start();

            while (!socket.isClosed()) {
                String message;
                if (console != null) {
                    // Using console for better input handling
                    message = readConsoleInput(console, client.getId(), inputBuffer);
                } else {
                    // Fallback to Scanner
                    System.out.print(client.getId() + ": ");
                    message = input.nextLine();
                }

                if (message != null && !message.trim().isEmpty()) {
                    // Clear the input line
                    System.out.print(MOVE_TO_LINE_START + CLEAR_LINE);

                    // Reset buffer
                    inputBuffer.setCurrentInput("");

                    // send message to the server
                    writer.println(client.getId() + ": " + message);
                }
            }

            if (input != null) {
                input.close();
            }
            server.close();
        }
    }
}
