import java.util.concurrent.ConcurrentHashMap;

class UserListMap {

    public record UserInfo(String ipAddress, int port, Boolean isCoordinator) {
    }

    // Thread-safe map
    private final ConcurrentHashMap<String, UserInfo> userMap = new ConcurrentHashMap<>();

    public void addUser(String username, String ip, Integer port, Boolean isCoordinator) {
        userMap.put(username, new UserInfo(ip, port, isCoordinator));
    }

    public UserInfo removeUser(String username) {
        if (username == null || username.trim() == "") {
            return null;
        }

        return userMap.remove(username);
    }

    public UserInfo getUser(String username) {
        return userMap.get(username);
    }

    public String getMemberList() {
        StringBuilder userListMessage = new StringBuilder();

        userMap.forEach((key, value) -> {
            String memberInfo = "user: " + key + " with ip: " + value.ipAddress() + ", and port: " + value.port()
                    + "\n";
            userListMessage.append(memberInfo);
        });

        return userListMessage.toString();
    }

    public Integer size() {
        return userMap.size();
    }

    public void print() {

        System.out.println("User list:");

        userMap.forEach((key, value) -> {
            System.out.println("  " + key);
        });
    }
}
