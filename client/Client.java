import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class Client {

    // ANSI escape sequences for moving the cursor and clearing lines.
    private static final String ANSI_UP_ONE = "\033[A";
    private static final String ANSI_CLEAR_LINE = "\033[K";

    public static class ClientConfig {
        private String targetIp;
        private Integer port;
        private String id;
        private String errorMessage;

        private ClientConfig(String targetIp, Integer port, String id, String errorMessage) {
            this.targetIp = targetIp;
            this.port = port;
            this.id = id;
            this.errorMessage = errorMessage;
        }

        public static ClientConfig build(FlagHandler flagHandler) {
            String targetIp = null;
            Integer port = null;
            String id = null;
            String errorMessage = "";

            if (flagHandler.hasFlag("target-ip")) {
                targetIp = flagHandler.getValue("target-ip");
            } else {
                errorMessage += "IP address is required\n";
            }

            if (flagHandler.hasFlag("port")) {
                String portStr = flagHandler.getValue("port");
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    errorMessage = "Invalid port number: " + port;
                }
            } else {
                errorMessage += "Port is required\n";
            }

            if (flagHandler.hasFlag("id")) {
                id = flagHandler.getValue("id");
            } else {
                id = "default_id";
            }

            if (!errorMessage.isEmpty()) {
                return new ClientConfig(null, null, null, errorMessage);
            }

            return new ClientConfig(targetIp, port, id, null);
        }

        public String getTargetIp() {
            return targetIp;
        }

        public Integer getPort() {
            return port;
        }

        public String getId() {
            return id;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

    }

    private static void handleServerMessage(String msg, String clientId, String clientMsg) {

        if (msg.equals("MSG-CONFIRMED")) { // confirmation of message sent
            // Move cursor up one line, go to begining of line, clear line, reprint
            System.out.print(ANSI_UP_ONE + "\r" + ANSI_CLEAR_LINE + clientId + ": " + clientMsg + " ✓\n");
        } else {
            System.out.println(msg);
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
                "Joining server at: " + client.getTargetIp() + ":" + client.getPort() + " as " + client.getId());

        try (Socket socket = new Socket(client.getTargetIp(), client.getPort())) {

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner server = new Scanner(socket.getInputStream());
            Scanner input = new Scanner(System.in);

            if (server.hasNextLine()) {
                String serverRes = server.nextLine();
                System.out.println(serverRes);
            }

            while (socket.isConnected() && !socket.isClosed()) {
                // user messag prompt

                String message = "";
                if (socket.isConnected()) {
                    System.out.print(client.getId() + ": ");
                    message = input.nextLine();
                }

                // send message to the server
                writer.println(client.getId() + ":" + message);

                // wait for response
                if (server.hasNextLine()) {
                    String serverRes = server.nextLine();
                    handleServerMessage(serverRes, client.getId(), message);
                } else {
                    System.out.println("Lost connection to server");
                    break;
                }
            }

            input.close();
            server.close();
        }
    }
}
