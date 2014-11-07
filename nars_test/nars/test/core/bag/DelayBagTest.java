/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.core.bag;

import nars.core.NAR;
import nars.core.build.Neuromorphic;
import nars.storage.DelayBag;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class DelayBagTest {
    
    @Test 
    public void testIO() {
        NAR n = NAR.build(Neuromorphic.class);

        DelayBag b = new DelayBag(1000);
        
        b.setMemory(n.memory);
        
        assertTrue(n.memory.concepts!=null);
        
    }
}
