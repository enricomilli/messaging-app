import java.util.concurrent.ConcurrentHashMap;

class UserListMap {
    // Custom record for IP and Port (Immutable)
    public record UserInfo(String ipAddress, int port, Boolean isCoordinator) {
    }

    // Thread-safe map with custom value object
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
