import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class Server {
    private static Integer port = 8080;
    // private Boolean hasCoordinator = false;

    public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(port)) {
            InetAddress address = listener.getInetAddress();
            System.out.printf("Server running on address: %s:%s\n", address.toString(), port.toString());

            while (true) {
                try {
                    Socket socket = listener.accept();
                    Connection conn = new Connection(socket);
                    Thread connectionHandler = new Thread(conn);
                    connectionHandler.start();

                } catch (IOException err) {
                    System.err.println("error accepting conn:" + err);
                }

            }

        }

    };

};
