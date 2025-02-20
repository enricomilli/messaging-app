import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class Client {

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

    public static void main(String[] args) throws IOException {
        FlagHandler flagHandler = new FlagHandler(args);

        ClientConfig client = ClientConfig.build(flagHandler);
        if (client.getErrorMessage() != null) {
            System.out.println("Error occurred: \n" + client.getErrorMessage());
            return;
        }

        System.out.println(
                "Joining server at: " + client.getTargetIp() + ":" + client.getPort() + " as " + client.getId());

        try (Socket socket = new Socket(client.targetIp, client.port)) {

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner server = new Scanner(socket.getInputStream());
            Scanner input = new Scanner(System.in);

            while (!socket.isClosed()) {
                if (!server.hasNextLine()) {
                    server.close();
                    input.close();
                    break;
                }

                String serverRes = server.nextLine();
                System.out.println(serverRes);

                System.out.print(client.getId() + ": ");
                String message = input.nextLine();
                writer.println(message);
            }

            input.close();
            server.close();
        }
    }
}
