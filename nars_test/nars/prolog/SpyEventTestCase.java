package nars.prolog;

import junit.framework.TestCase;
import nars.prolog.event.SpyEvent;

public class SpyEventTestCase extends TestCase {
	
	public void testToString() {
		String msg = "testConstruction";
		SpyEvent e = new SpyEvent(new Prolog(), msg);
		assertEquals(msg, e.toString());
	}

}
