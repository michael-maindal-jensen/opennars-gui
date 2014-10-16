package nars.core.build;

import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.core.Memory.Timing;
import nars.core.NAR;
import nars.core.NARBuilder;
import nars.core.Param;
import nars.core.Parameters;
import nars.core.control.SequentialMemoryCycle;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.plugin.mental.TemporalParticlePlanner;
import nars.storage.AbstractBag;
import nars.storage.Bag;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class DefaultNARBuilder extends NARBuilder implements ConceptBuilder {

    
    public int taskLinkBagLevels;
    
    /** Size of TaskLinkBag */
    public int taskLinkBagSize;
    
    public int termLinkBagLevels;
    
    /** Size of TermLinkBag */
    public int termLinkBagSize;
    
    /** determines maximum number of concepts */
    private int conceptBagSize;    

    /** Size of TaskBuffer */
    private int taskBufferSize = 10;
    private Memory.Timing timing = Timing.Iterative;
    
    
    public DefaultNARBuilder() {
        super();
        
        setConceptBagLevels(100);
        setConceptBagSize(1000);        
        
        setTaskLinkBagLevels(100);
        setTaskLinkBagSize(20);

        setTermLinkBagLevels(100);
        setTermLinkBagSize(100);
        
        setTaskBufferSize(10);
    }

    @Override
    public Param newParam() {
        Param p = new Param();
        p.setTiming(timing);
        p.noiseLevel.set(100);
        
        //Cycle control
        p.cycleMemory.set(1);
        p.cycleInputTasks.set(1);

        p.decisionThreshold.set(0.30);
        
        p.duration.set(5);
        p.conceptForgetDurations.set(2.0);
        p.taskCycleForgetDurations.set(4.0);
        p.beliefForgetDurations.set(10.0);
        p.newTaskForgetDurations.set(2.0);
                
        p.conceptBeliefsMax.set(7);
        p.conceptQuestionsMax.set(5);
        
        p.contrapositionPriority.set(30);
                
        p.termLinkMaxReasoned.set(3);
        p.termLinkMaxMatched.set(10);
        p.termLinkRecordLength.set(10);
        
        return p;
    }

    @Override
    public NAR build() {
        NAR n = super.build();
        
        //the only plugin which is dependent on a parameter
        //because it enriches NAL8 performance a lot:
        if(Parameters.TEMPORAL_PARTICLE_PLANNER) {
            TemporalParticlePlanner planner=new TemporalParticlePlanner();
            n.addPlugin(planner);
        }
        
        return n;
    }
    
    
    
    @Override
    public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c) {
        return new SequentialMemoryCycle(newConceptBag(p), c);
    }

    @Override
    public ConceptBuilder getConceptBuilder() {
        return this;
    }

    @Override
    public Concept newConcept(Term t, Memory m) {        
        AbstractBag<TaskLink> taskLinks = new Bag<>(getTaskLinkBagLevels(), getTaskLinkBagSize(), m.param.taskCycleForgetDurations);
        AbstractBag<TermLink> termLinks = new Bag<>(getTermLinkBagLevels(), getTermLinkBagSize(), m.param.beliefForgetDurations);
        
        return new Concept(t, taskLinks, termLinks, m);        
    }

    
    protected AbstractBag<Concept> newConceptBag(Param p) {
        return new Bag(getConceptBagLevels(), getConceptBagSize(), p.conceptForgetDurations);
    }

    @Override
    public AbstractBag<Task> newNovelTaskBag(Param p) {
        return new Bag<>(getConceptBagLevels(), getTaskBufferSize(), p.newTaskForgetDurations);
    }
 
    
    
    public int getConceptBagSize() { return conceptBagSize; }    
    public DefaultNARBuilder setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    /** Level granularity in Bag, usually 100 (two digits) */    
    private int conceptBagLevels;
    public int getConceptBagLevels() { return conceptBagLevels; }    
    public DefaultNARBuilder setConceptBagLevels(int bagLevels) { this.conceptBagLevels = bagLevels; return this;  }
        
    /**
     * @return the taskLinkBagLevels
     */
    public int getTaskLinkBagLevels() {
        return taskLinkBagLevels;
    }
       
    public DefaultNARBuilder setTaskLinkBagLevels(int taskLinkBagLevels) {
        this.taskLinkBagLevels = taskLinkBagLevels;
        return this;
    }

    public void setTaskBufferSize(int taskBufferSize) {
        this.taskBufferSize = taskBufferSize;
    }

    public int getTaskBufferSize() {
        return taskBufferSize;
    }
    
    

    public int getTaskLinkBagSize() {
        return taskLinkBagSize;
    }

    public DefaultNARBuilder setTaskLinkBagSize(int taskLinkBagSize) {
        this.taskLinkBagSize = taskLinkBagSize;
        return this;
    }

    public int getTermLinkBagLevels() {
        return termLinkBagLevels;
    }

    public DefaultNARBuilder setTermLinkBagLevels(int termLinkBagLevels) {
        this.termLinkBagLevels = termLinkBagLevels;
        return this;
    }

    public int getTermLinkBagSize() {
        return termLinkBagSize;
    }

    public DefaultNARBuilder setTermLinkBagSize(int termLinkBagSize) {
        this.termLinkBagSize = termLinkBagSize;
        return this;
    }

    
    public static class CommandLineNARBuilder extends DefaultNARBuilder {
        private final Param param;

        @Override public Param newParam() {        
            return param;
        }

        public CommandLineNARBuilder(String[] args) {
            super();

            param = super.newParam();

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--silence".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);                
                    param.noiseLevel.set(100-sl);
                }
                if ("--noise".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);                
                    param.noiseLevel.set(sl);
                }            
            }        
        }



        /**
         * Decode the silence level
         *
         * @param param Given argument
         * @return Whether the argument is not the silence level
         */
        public static boolean isReallyFile(String param) {
            return !"--silence".equals(param);
        }
    }
    
    public DefaultNARBuilder realTime() {
        timing = Timing.Real;
        return this;
    }
    public DefaultNARBuilder simulationTime() {
        timing = Timing.Simulation;
        return this;
    }

    /* ---------- initial values of run-time adjustable parameters ---------- */
//    /** Concept decay rate in ConceptBag, in [1, 99]. */
//    private static final int CONCEPT_CYCLES_TO_FORGET = 10;
//    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. */
//    private static final int TASK_LINK_CYCLES_TO_FORGET = 20;
//    /** TermLink decay rate in TermLinkBag, in [1, 99]. */
//    private static final int TERM_LINK_CYCLES_TO_FORGET = 50;        
//    /** Task decay rate in TaskBuffer, in [1, 99]. */
//    private static final int NEW_TASK_FORGETTING_CYCLE = 10;

}
