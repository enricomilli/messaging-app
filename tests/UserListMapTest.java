import static org.junit.Assert.*;
import org.junit.Test;

public class UserListMapTest {

    @Test
    public void testAddAndGetUser() {
        UserListMap ulm = new UserListMap();
        ulm.addUser("user1", "127.0.0.1", 8080, false);
        UserListMap.UserInfo info = ulm.getUser("user1");

        assertNotNull("User should be added", info);
        assertEquals("127.0.0.1", info.ipAddress());
        assertEquals(8080, info.port());
        assertFalse("User should not be marked as coordinator", info.isCoordinator());
    }

    @Test
    public void testRemoveUser() {
        UserListMap ulm = new UserListMap();
        ulm.addUser("user2", "192.168.1.1", 9090, true);
        UserListMap.UserInfo removed = ulm.removeUser("user2");

        assertNotNull("Removed user info should not be null", removed);
        assertNull("User should no longer exist", ulm.getUser("user2"));
    }

    @Test
    public void testGetMemberList() {
        UserListMap ulm = new UserListMap();
        ulm.addUser("userA", "10.0.0.1", 3000, false);
        ulm.addUser("userB", "10.0.0.2", 3001, true);
        String members = ulm.getMemberList();

        assertTrue("Member list should contain userA", members.contains("userA"));
        assertTrue("Member list should contain userB", members.contains("userB"));
    }
}
