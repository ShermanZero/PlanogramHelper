
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Admin
 */
public class LauncherTest {
    
    @Test
    public void testDevMode() {
        assertEquals(true, Boolean.parseBoolean(System.getProperty("dev")));
    }
}
