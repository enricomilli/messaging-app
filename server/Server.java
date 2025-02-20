import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

class Server {
    private static Integer port = 8080;

    private static void handleCommands(Scanner reader, PrintWriter writer, String command) {

        if (command.equals("/leave")) {
            System.out.println("client disconnected");
            writer.println("bye bye!");
            reader.close();
        } else {
            writer.println("that command is not recognized");
        }

    }

    private String getCoordinatorMsg(ServerSocket listener) {

        return "You are the coordinator!";
    }

    public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(port)) {
            InetAddress address = listener.getInetAddress();
            System.out.printf("Server running on address: %s:%s\n", address.toString(), port.toString());

            while (true) {
                try (Socket socket = listener.accept()) {
                    InetAddress clientAddr = socket.getInetAddress();

                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    Scanner reader = new Scanner(socket.getInputStream());

                    writer.println("connected! send a message anytime.");

                    while (!socket.isClosed()) {

                        if (!reader.hasNextLine()) {
                            reader.close();
                            System.out.println("client disconnected");
                            break;
                        }

                        String message = reader.nextLine().trim();

                        if (message.isEmpty()) {
                            writer.println("make sure you write something!");
                        } else if (message.startsWith("/")) {
                            handleCommands(reader, writer, message);
                        } else {
                            System.out.println("client message: '" + message + "'");
                            writer.println("message received");
                        }

                    }

                }

            }

        }

    };

};
