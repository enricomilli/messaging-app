
import static org.junit.Assert.*;
import org.junit.Test;

public class ClientConfigTest {

    @Test
    public void testValidClientConfig() {
        String[] args = {"--target-ip", "127.0.0.1", "--port", "8080", "--id", "tester"};
        FlagHandler fh = new FlagHandler(args);
        ClientConfig config = ClientConfig.build(fh);

        assertNull("No error message expected", config.getErrorMessage());
        assertEquals("127.0.0.1", config.getTargetIp());
        assertEquals(Integer.valueOf(8080), config.getTargetPort());
        assertEquals("tester", config.getId());
    }

    @Test
    public void testMissingIDGeneratesUUID() {
        String[] args = {"--target-ip", "192.168.0.1", "--port", "9090"};
        FlagHandler fh = new FlagHandler(args);
        ClientConfig config = ClientConfig.build(fh);

        assertNull("No error message expected when id is missing", config.getErrorMessage());
        assertEquals("192.168.0.1", config.getTargetIp());
        assertEquals(Integer.valueOf(9090), config.getTargetPort());
        assertNotNull("A randomly generated id should not be null", config.getId());
        assertFalse("Generated id should not be empty", config.getId().isEmpty());
    }

    @Test
    public void testMissingIPShouldProduceError() {
        String[] args = {"--port", "8080"};
        FlagHandler fh = new FlagHandler(args);
        ClientConfig config = ClientConfig.build(fh);

        assertNotNull("Error message expected when IP is missing", config.getErrorMessage());
    }
}
