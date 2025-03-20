import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

class Server {
    private static Integer port = 8080;
    private static final MessagesList messagesList = new MessagesList();
    private static final AtomicInteger numOfConnections = new AtomicInteger();
    private static final UserListMap userList = new UserListMap();

    public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(port)) {
            InetAddress address = listener.getInetAddress();
            System.out.printf("Server running on address: %s:%s\n", address.toString(), port.toString());

            while (true) {
                try {
                    Socket socket = listener.accept();
                    Connection conn = new Connection(socket, messagesList, numOfConnections, userList);
                    Thread connectionHandler = new Thread(conn);
                    connectionHandler.start();

                } catch (IOException err) {
                    System.err.println("error with connection:" + err);
                }

            }

        }

    };

};
