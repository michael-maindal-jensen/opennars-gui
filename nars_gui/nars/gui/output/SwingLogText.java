package nars.gui.output;

import java.awt.Color;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.gui.NARSwing;
import nars.io.Output.OUT;


public class SwingLogText extends SwingText  {
    private final NAR nar;
    public boolean showStamp = false;
    final Deque<LogLine> pendingDisplay = new ConcurrentLinkedDeque<>();
    private JScrollPane scroller;


    
    public static class LogLine {
        public final Class c;
        public final Object o;

        public LogLine(Class c, Object o) {
            this.c = c;
            this.o = o;
        }
        
    }
    
    

    public SwingLogText(NAR n) {        
        super();
        
        this.nar = n;
        
    }

//    @Override
//    public void paint(Graphics g) {
//        super.paint(g); //To change body of generated methods, choose Tools | Templates.
//        if (isVisible()) {
//
////            try {
////
////                TextUI mapper = getUI();
////
////                
////                Rectangle r = mapper.modelToView(this, getCaretPosition());
////                System.out.println("caret: " + r);
////
////            } catch (Exception e) {
////
////                System.err.println("Problem painting cursor");
////
////            }
//
//            //scrollUpdate();
//        }
//
//    }
    
    void setScroller(JScrollPane scroller) {
        this.scroller = scroller;
        /*scroller.getViewport().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    
                    //scrollUpdate();

                }
            });*/
    }

//    protected void scrollUpdate() {
//        int docLen = doc.getLength();
//        if (docLen > 0) {
//            //JViewport viewport = (JViewport) e.getSource();
//            Rectangle viewRect = scroller.getViewport().getViewRect();
//
//            Point p = viewRect.getLocation();
//            int startIndex = viewToModel(p);
//
//            p.x += viewRect.width;
//            p.y += viewRect.height;
//            int endIndex = viewToModel(p);
//
//            for (int offset = endIndex; offset < startIndex;) {
//                try {
//                    //System.out.println(" " + offset);
//                    
//                    onLineVisible(offset);
//                    
//                    offset = Utilities.getRowStart(SwingLogText.this, offset) - 1;
//                    
//                } catch (BadLocationException ex) {
//                    Logger.getLogger(SwingLogText.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            //System.out.println("< -- (" + endIndex + ", " + startIndex);
//        }        
//    }
    
    protected void onLineVisible(int offset) { }
    
    
    public void output(final Class c, final Object o) {                
        pendingDisplay.addLast(new LogLine(c, o));
                
        if (pendingDisplay.size() == 1) {
            //only invoke update after the first has been added
            SwingUtilities.invokeLater(update);
        }
    }
    
    public final Runnable update = new Runnable() {
        
        //final Rectangle bottom = new Rectangle(0,Integer.MAX_VALUE-1,1,1);        
        
        @Override public void run() {
            
            while (pendingDisplay.size() > 0) {
                LogLine l = pendingDisplay.removeFirst();
                print(l.c, l.o);
            
            }
                        
            limitBuffer();                        

            /*try {
                //scrollRectToVisible(bottom);
            }
            catch (Exception e) { } */
        }
    };
    
//    public class TaskIcon extends NCanvas {
//
//        public TaskIcon() {
//            super();
//            setMaximumSize(new Dimension(50,10));
//            setPreferredSize(new Dimension(50,10));
//            setSize(50,10);
//            
//            Graphics2D g = getBufferGraphics();
//            
//            showBuffer(g);
//        }
//        
//        
//        
//    }
    
    protected int print(Class c, Object o)  {        

        float priority = 1f;

        
        
        if (c!=OUT.class) {
            //pad the channel name to max 6 characters, right aligned
            
            String n = c.getSimpleName();
            n = n.substring(0,Math.min(6, n.length()));
            switch (n.length()) {
                case 0: break;
                case 1: n = "     " + n; break;
                case 2: n = "    " + n; break;
                case 3: n = "   " + n; break;
                case 4: n = "  " + n; break;
                case 5: n = " " + n; break;                    
            }
            Color chanColor = NARSwing.getColor(c.getClass().hashCode(), 0.8f, 0.8f);
            print(chanColor, n);
        }
        
        else {
            if (o instanceof Task) {
                Task t = (Task)o;
                Sentence s = t.sentence;
                if (s!=null) {
                    priority = t.budget.getPriority();
                    printColorBlock(LogPanel.getPriorityColor(priority), "  ");
                
                    TruthValue tv = s.truth;
                    if (tv!=null) {                    
                        printColorBlock(LogPanel.getFrequencyColor(tv.getFrequency()), "  ");
                        printColorBlock(LogPanel.getConfidenceColor(tv.getConfidence()), "  ");                        
                    }
                    else if ( t.getBestSolution()!=null) {
                        printColorBlock(LogPanel.getStatementColor('=', priority), "    ");
                    }
                    else {                        
                        printColorBlock(LogPanel.getStatementColor(s.punctuation, priority), "    ");                   
                    }
                }
            }
        }        
        
        float tc = 0.75f + 0.25f * priority;
        Color textColor = new Color(tc,tc,tc);
        
        CharSequence text = LogPanel.getText(c, o, showStamp, nar);
        StringBuilder sb = new StringBuilder(text.length()+2);
        sb.append(' ');
        if (text.length() > maxLineWidth)
            sb.append(text.subSequence(0,maxLineWidth));
        else
            sb.append(text);

        if (sb.charAt(sb.length()-1)!='\n')
            sb.append('\n');
                
                
        print(textColor, sb.toString());
        
        return doc.getLength();
        
    }
    

}
