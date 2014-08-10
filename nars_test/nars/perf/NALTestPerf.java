package nars.perf;

import nars.util.ContinuousBagNARBuilder;
import java.util.Collection;
import nars.core.DefaultNARBuilder;
import nars.core.NAR;
import nars.test.core.NALTest;
import static nars.test.core.NALTest.newNAR;


/** tests performance of NAL, but can also uncover bugs when NAL runs with a heavy and long load */
public class NALTestPerf  {
    
    public static void perfNAL(final String path, final int extraCycles, int repeats, int warmups) {
        perfNAL(newNAR(), path, extraCycles, repeats, warmups, true);
    }
    
    public static double perfNAL(final NAR n, final String path, final int extraCycles, int repeats, int warmups, boolean gc) {
        
        final String example = NALTest.getExample(path);
        
        Performance p = new Performance(path, repeats, warmups, gc) {
            long totalCycles;
            
            @Override
            public void init() {
                
                totalCycles = 0;
            }

            @Override
            public void run(boolean warmup) {
                try {
                    n.reset();
                    n.addInput(example);
                    n.finish(extraCycles);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
                
                totalCycles += n.getTime();
            }

            @Override
            public Performance print() {                
                super.print();
                System.out.print(", " + df.format(getCycleTimeMS() / totalCycles * 1000.0) + " ns/cycle, " + (((float)totalCycles)/(warmups+repeats)) + " cycles/run");
                return this;
                
            }
            @Override
            public Performance printCSV(boolean finalComma) {
                super.printCSV(true);
                System.out.print(df.format(getCycleTimeMS() / totalCycles * 1000.0) + ", " + (((float)totalCycles)/(warmups+repeats)));
                if (finalComma)
                    System.out.print(", ");
                return this;
                
            }

        };
        p.print();
        System.out.println();

        /*p.printCSV(false);
        System.out.println();*/
        
        return p.getCycleTimeMS();
                   
    }
    
    public static void test(NAR n) {
        int repeats = 1;
        int warmups = 0;
        int extraCycles = 5000;

        Collection c = NALTest.params();
        double totalTime = 0;
        for (Object o : c) {
            String examplePath = (String)((Object[])o)[0];
            totalTime += perfNAL(n,examplePath,extraCycles,repeats,warmups,true);
        }
        System.out.println("\n\nTotal mean runtime (ms): " + totalTime);        
    }
    
    public static void main(String[] args) {
        
        NAR nc = new ContinuousBagNARBuilder().build();
        test(nc);

        NAR nd = new DefaultNARBuilder().build();
        test(nd);
        
        
    }

}
