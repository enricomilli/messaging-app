
class ClientConfig {
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
