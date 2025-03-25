
import static org.junit.Assert.*;
import org.junit.Test;

public class FlagHandlerTest {

    @Test
    public void testParsingWithEqualSign() {
        String[] args = {"--target-ip=127.0.0.1", "--port=8080", "--id=testUser"};
        FlagHandler fh = new FlagHandler(args);

        assertTrue("target-ip flag present", fh.hasFlag("target-ip"));
        assertEquals("127.0.0.1", fh.getValue("target-ip"));

        assertTrue("port flag present", fh.hasFlag("port"));
        assertEquals("8080", fh.getValue("port"));

        assertTrue("id flag present", fh.hasFlag("id"));
        assertEquals("testUser", fh.getValue("id"));
    }

    @Test
    public void testParsingSpaceSeparatedFlags() {
        String[] args = {"-t", "192.168.1.1", "-p", "9090", "-i", "user123"};
        FlagHandler fh = new FlagHandler(args);

        // long-form equivalent is used by the flagPairs mapping
        assertTrue("target-ip flag present", fh.hasFlag("target-ip"));
        assertEquals("192.168.1.1", fh.getValue("target-ip"));

        assertTrue("port flag present", fh.hasFlag("port"));
        assertEquals("9090", fh.getValue("port"));

        assertTrue("id flag present", fh.hasFlag("id"));
        assertEquals("user123", fh.getValue("id"));
    }

    @Test
    public void testMissingFlags() {
        String[] args = {"--foo", "bar"};
        FlagHandler fh = new FlagHandler(args);

        assertFalse("target-ip flag missing", fh.hasFlag("target-ip"));
        assertNull("No value returned for missing target-ip", fh.getValue("target-ip"));
    }
}
