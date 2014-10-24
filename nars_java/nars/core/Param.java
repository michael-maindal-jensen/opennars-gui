package nars.core;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import nars.core.Memory.Forgetting;
import nars.core.Memory.Timing;
import nars.language.Interval.AtomicDuration;
import nars.util.meter.util.AtomicDouble;

/**
 * NAR Parameters which can be changed during runtime.
 */
public class Param implements Serializable {

    /** Silent threshold for task reporting, in [0, 100]. 
     *  Noise level = 100 - silence level; noise 0 = always silent, noise 100 = never silent
     */
    public final AtomicInteger noiseLevel = new AtomicInteger();
    
    /** 
       Past/future tense usage convention, how far away "past" and "future" is from "now", in cycles.  
       (Cycles per duration)
       The range of "now" is [-DURATION, DURATION];      */
    public final AtomicDuration duration = new AtomicDuration();

    protected Timing timing;
    protected Forgetting forgetting;
    

    
    /** can return result multiplied by duration */
    public final class AtomicDurations extends AtomicDouble {
        
        /** gets # of cycles, which is # durations * cycles/duration */
        public float getCycles() {
            return super.floatValue() * duration.floatValue();
        }
    }
    
    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE 
     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
     *  Lower means faster rate of decay.
     */
    public final AtomicDurations conceptForgetDurations = new AtomicDurations();
    
    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    public final AtomicDurations beliefForgetDurations = new AtomicDurations();
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    public final AtomicDurations taskForgetDurations = new AtomicDurations();
    
    public final AtomicDurations newTaskForgetDurations = new AtomicDurations();

    
    /** Minimum expectation for a desire value. 
     *  the range of "now" is [-DURATION, DURATION]; */
    public final AtomicDouble decisionThreshold = new AtomicDouble();
    
    /** How many inputs to process each cycle */
    public final AtomicInteger cycleInputTasks = new AtomicInteger();

    /** How many memory working cycles to process each cycle */
    public final AtomicInteger cycleMemory = new AtomicInteger();

    /** How many concepts to fire each cycle */
    public final AtomicInteger cycleConceptsFired = new AtomicInteger();
    
    /** contraposition should have much lower priority considering the flood of implications coming from temporal knowledge.  a lower value means more frequent, must be > 0 */    
    public final AtomicDouble contrapositionPriority = new AtomicDouble(/*30 was the default*/);

    
    /** Maximum TermLinks checked for novelty for each TaskLink in TermLinkBag */
    public final AtomicInteger termLinkMaxMatched = new AtomicInteger();
            
    
//    //let NARS use NARS+ ideas (counting etc.)
//    public final AtomicBoolean experimentalNarsPlus = new AtomicBoolean();
//
//    //let NARS use NAL9 operators to perceive its own mental actions
//    public final AtomicBoolean internalExperience = new AtomicBoolean();
    
    //these two are AND-coupled:
    //when a concept is important and exceeds a syntactic complexity, let NARS name it: 
    public final AtomicInteger abbreviationMinComplexity = new AtomicInteger();
    public final AtomicDouble abbreviationMinQuality = new AtomicDouble();
    
    /** Maximum TermLinks used in reasoning for each Task in Concept */
    public final AtomicInteger termLinkMaxReasoned = new AtomicInteger();

    /** Record-length for newly created TermLink's */
    public final AtomicInteger termLinkRecordLength = new AtomicInteger();
    
    /** Maximum number of beliefs kept in a Concept */
    public final AtomicInteger conceptBeliefsMax = new AtomicInteger();
    
    /** Maximum number of questions kept in a Concept */
    public final AtomicInteger conceptQuestionsMax = new AtomicInteger();

    /** Maximum number of goals kept in a Concept */
    public final AtomicInteger conceptGoalsMax = new AtomicInteger();
    
    public void setTiming(Timing time) {
        this.timing = time;
    }

    public Timing getTiming() {
        return timing;
    }

    public Forgetting getForgetMode() {
        return forgetting;
    }

    public void setForgetMode(Forgetting forgetMode) {
        this.forgetting = forgetMode;
    }
    
    
    
    
}
