package nars.test.util;

import nars.storage.Bag;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author me
 */
public class AdaptiveContinuousBagTest {

    @Test
    public void test() {
        assertEquals(0, Bag.bin(0, 10));
        assertEquals(1, Bag.bin(0.1f, 10));
        assertEquals(9, Bag.bin(0.9f, 10));
        assertEquals(9, Bag.bin(0.925f, 10));
        assertEquals(10, Bag.bin(0.975f, 10));
        assertEquals(10, Bag.bin(1.0f, 10));
        
        
        assertEquals(0, Bag.bin(0f, 9));
        assertEquals(1, Bag.bin(0.1f, 9));
        assertEquals(8, Bag.bin(0.9f, 9));
        assertEquals(9, Bag.bin(1.0f, 9));
    }

}
