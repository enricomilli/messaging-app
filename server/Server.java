import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

class Server {
    private static Integer port = 8080;

    private static void handleCommands(Scanner in, PrintWriter writer, String command) {

        if (command.equals("/leave")) {
            System.out.println("closing this client");
            writer.println("bye bye!");
            in.close();
        } else {
            writer.println("that command is not recognized");
        }
    }

    public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(port)) {
            InetAddress address = listener.getInetAddress();
            System.out.printf("Server running on address: %s:%s\n", address.toString(), port.toString());

            while (true) {
                try (Socket socket = listener.accept()) {

                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    Scanner in = new Scanner(socket.getInputStream());

                    while (socket.isConnected()) {
                        String message = in.nextLine().trim();
                        if (message.isEmpty()) {
                            writer.println("make sure you write something!");
                            continue;
                        }

                        System.out.println("client message: '" + message + "'");
                        writer.println("message received");

                        if (message.startsWith("/")) {
                            handleCommands(in, writer, message);
                            continue;
                        }

                    }

                    in.close();

                }

            }

        }

    };

};
