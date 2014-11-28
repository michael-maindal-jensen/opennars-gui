/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.dialog;

import automenta.vivisect.swing.NWindow;
import nars.core.NAR;
import nars.core.build.Default;
import nars.gui.NARSwing;
import nars.gui.input.KeyboardInputPanel;

/**
 *
 * @author me
 */
public class KeyboardInputExample {
    
    public static void main(String[] args) {
        //NAR n = NAR.build(new Neuromorphic().realTime());
        //NAR n = NAR.build(new Default().realTime());
        //n.param.duration.set(100);
        
        NAR n = new NAR(new Default());
        
        
                
        new NARSwing(n).themeInvert();

        new NWindow("Direct Keyboard Input", new KeyboardInputPanel(n)).show(300, 100, false);
        
        n.start(100, 5);
        
        
    }
}
