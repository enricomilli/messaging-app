import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

class Coordinator {
    private UserListMap userList;
    private MessagesCoordinator messagesCoordinator;
    private Timer heartbeatTimer;

    public Coordinator(UserListMap userList, MessagesCoordinator messagesCoordinator) {
        this.userList = userList;
        this.messagesCoordinator = messagesCoordinator;
        startHeartbeat();
    }

    // Start a periodic check every 20 seconds to check on users
    private void startHeartbeat() {
        heartbeatTimer = new Timer(true); // true makes it run on a separate thread
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // Get a snapshot of the listeners from the MessagesCoordinator
                List<MessagesCoordinator.MessageListener> listeners = messagesCoordinator.getListeners();
                for (MessagesCoordinator.MessageListener listener : listeners) {
                    // Skip the coordinator itself
                    if (listener.isCoordinator()) continue;
                    // If the listener is not alive, remove it
                    if (!listener.isAlive()) {
                        String uid = listener.getUserId();
                        userList.removeUser(uid);
                        messagesCoordinator.removeListener(listener);
                        messagesCoordinator.addMessage("[Server] " + uid + " removed due to connectivity issues.");
                    }
                }
            }
        }, 20000, 20000); // Initial delay 20 sec, period 20 sec
    }

    public void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
        }
    }

    // Handle incoming commands from members
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

    // Format: /message targetUser message
    private void handleSendPrivateMessage(String requestIp, Integer requestPort, String command) {

        String[] splitCmd = command.split(" ", 3);
        if (splitCmd.length < 3) {
            messagesCoordinator.sendMsgToIp(requestIp, requestPort,
                "[Coordinator] Error in message formatting. It should follow this format: /message targetUser your message here");
            return;
        }

        String receivingUserId = splitCmd[1];
        if (receivingUserId == null || receivingUserId.trim().isEmpty()) {
            messagesCoordinator.sendMsgToIp(requestIp, requestPort, "[Coordinator] Target username is empty");
            return;
        }

        String msgContent = splitCmd[2];
        if (msgContent == null || msgContent.trim().isEmpty()) {
            messagesCoordinator.sendMsgToIp(requestIp, requestPort, "[Coordinator] Message is empty");
            return;
        }

        UserListMap.UserInfo targetUserInfo = userList.getUser(receivingUserId);
        if (targetUserInfo == null) {
            messagesCoordinator.sendMsgToIp(requestIp, requestPort, "[Coordinator] Username not found");
            return;
        }

        String finalMessage = "Received private message:\n" +
                messagesCoordinator.getUsernameByAddress(requestIp, requestPort) + ": " + msgContent + "\nEnd of message";

        messagesCoordinator.sendMsgToIp(targetUserInfo.ipAddress(), targetUserInfo.port(), finalMessage);
    }

    private void handleListMembersCommand(String requestIp, Integer requestPort) {
        String msgToWrite = userList.getMemberList();
        messagesCoordinator.sendMsgToIp(requestIp, requestPort, msgToWrite);
    }
}
