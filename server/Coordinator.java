
class Coordinator {
    private UserListMap userList;
    private MessagesCoordinator messagesCoordinator;


    public Coordinator(UserListMap userList, MessagesCoordinator messagesCoordinator) {
        this.userList = userList;
        this.messagesCoordinator = messagesCoordinator;
    }

    public String handleCommand(String requestIp, Integer requestPort, String command) {

        if (command.startsWith("/members") || command.startsWith("/list")) {
            handleListMembersCommand(requestIp, requestPort);
        }

        return null;
    }

    private void handleListMembersCommand(String requestIp, Integer requestPort) {
        String msgToWrite = userList.getMemberList();
        messagesCoordinator.sendMsgToIp(requestIp, requestPort, msgToWrite);
    }

}
