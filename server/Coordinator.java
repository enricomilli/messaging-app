

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
        } else if (command.startsWith("/message")) {
            // handle sending a private message
            handleSendPrivateMessage(requestIp, requestPort, command);
        } else {
            messagesCoordinator.sendMsgToIp(requestIp, requestPort, "[Coordinator] Command not recognized");
        }

        return null;
    }

    // First arguement after the command is the userId whos receiving the message
    private void handleSendPrivateMessage(String requestIp, Integer requestPort, String command) {

        // command format: /message targetUser message
        String[] splitCmd = command.split(" ", 3);
        if (splitCmd.length < 3) {
            messagesCoordinator.sendMsgToIp(requestIp, requestPort, "[Coordinator] Error in message formating\nIt should follow this format: /message targetUser your message here");
            return;
        }

        String receivingUserId = splitCmd[1];
        if (receivingUserId == null || receivingUserId.trim() == "") {
            messagesCoordinator.sendMsgToIp(requestIp, requestPort, "[Coordinator] Target username is empty");
            return;
        }

        String msgContent = splitCmd[2];
        if (msgContent == null || msgContent.trim() == "") {
            messagesCoordinator.sendMsgToIp(requestIp, requestPort, "[Coordinator] message is empty");
            return;
        }

        UserListMap.UserInfo targetUserInfo = userList.getUser(receivingUserId);
        if (targetUserInfo == null) {
            messagesCoordinator.sendMsgToIp(requestIp, requestPort, "[Coordinator] Username not found");
            return;
        }

        String finalMessage = "[Coordinator] Received private message from: " + messagesCoordinator.getUsernameByAddress(requestIp, requestPort) + "\n" + msgContent;

        messagesCoordinator.sendMsgToIp(targetUserInfo.ipAddress(), targetUserInfo.port(), finalMessage);
    }

    private void handleListMembersCommand(String requestIp, Integer requestPort) {
        String msgToWrite = userList.getMemberList();
        messagesCoordinator.sendMsgToIp(requestIp, requestPort, msgToWrite);
    }

}
