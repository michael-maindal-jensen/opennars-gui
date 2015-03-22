package nars.io.meter;

import java.io.Serializable;
import nars.core.Parameters;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.io.Symbols;
import nars.language.Inheritance;
import nars.language.SetExt;
import nars.language.SetInt;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.plugin.mental.InternalExperience;

/** emotional value; self-felt internal mental states; variables used to record emotional values */
public class EmotionMeter implements Serializable {

    /** average desire-value */
    private float happy;
    /** average priority */
    private float busy;

    public EmotionMeter() {
    }

    public EmotionMeter(float happy, float busy) {
        set(happy, busy);
    }

    public void set(float happy, float busy) {
        this.happy = happy;
        this.busy = busy;
    }

    public float happy() {
        return happy;
    }

    public float busy() {
        return busy;
    }

    public double lasthappy=-1;
    public void adjustHappy(float newValue, float weight, NAL nal) {
        //        float oldV = happyValue;
        happy += newValue * weight;
        happy /= 1.0f + weight;
        
        if(lasthappy!=-1) {
            float frequency=-1;
            if(happy>Parameters.HAPPY_EVENT_HIGHER_THRESHOLD && lasthappy<=Parameters.HAPPY_EVENT_HIGHER_THRESHOLD) {
                frequency=1.0f;
            }
            if(happy<Parameters.HAPPY_EVENT_LOWER_THRESHOLD && lasthappy>=Parameters.HAPPY_EVENT_LOWER_THRESHOLD) {
                frequency=0.0f;
            }
            if(frequency!=-1) { //ok lets add an event now
                Term predicate=SetInt.make(new Term("satisfied"));
                Term subject=new Term("SELF");
                Inheritance inh=Inheritance.make(subject, predicate);
                TruthValue truth=new TruthValue(1.0f,Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                Sentence s=new Sentence(inh,Symbols.JUDGMENT_MARK,truth,new Stamp(nal.memory));
                s.stamp.setOccurrenceTime(nal.memory.time());
                Task t=new Task(s,new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,Parameters.DEFAULT_JUDGMENT_DURABILITY,BudgetFunctions.truthToQuality(truth)));
                nal.addTask(t, "emotion");
                if(Parameters.REFLECT_META_HAPPY_GOAL) { //remind on the goal whenever happyness changes, should suffice for now
                    TruthValue truth2=new TruthValue(1.0f,Parameters.DEFAULT_GOAL_CONFIDENCE);
                    Sentence s2=new Sentence(inh,Symbols.GOAL_MARK,truth2,new Stamp(nal.memory));
                    s2.stamp.setOccurrenceTime(nal.memory.time());
                    Task t2=new Task(s2,new BudgetValue(Parameters.DEFAULT_GOAL_PRIORITY,Parameters.DEFAULT_GOAL_DURABILITY,BudgetFunctions.truthToQuality(truth2)));
                    nal.addTask(t2, "metagoal");
                    //this is a good candidate for innate belief for consider and remind:
                    Operator consider=nal.memory.getOperator("^consider");
                    Operator remind=nal.memory.getOperator("^remind");
                    Term[] arg=new Term[1];
                    arg[0]=inh;
                    if(InternalExperience.enabled && Parameters.CONSIDER_REMIND) {
                        Operation op_consider=Operation.make(consider, arg, true);
                        Operation op_remind=Operation.make(remind, arg, true);
                        Operation[] op=new Operation[2];
                        op[0]=op_remind; //order important because usually reminding something
                        op[1]=op_consider; //means it has good chance to be considered after
                        for(Operation o : op) {
                            TruthValue truth3=new TruthValue(1.0f,Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                            Sentence s3=new Sentence(o,Symbols.JUDGEMENT_MARK,truth3,new Stamp(nal.memory));
                            s3.stamp.setOccurrenceTime(nal.memory.time());
                            
                            //INTERNAL_EXPERIENCE_DURABILITY_MUL
                            BudgetValue budget=new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,Parameters.DEFAULT_JUDGMENT_DURABILITY,BudgetFunctions.truthToQuality(truth3));
                            budget.setPriority(budget.getPriority()*InternalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL);
                            budget.setDurability(budget.getPriority()*InternalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL);
                            Task t3=new Task(s3,budget);
                            nal.addTask(t3, "internal experience for consider and remind");
                        }
                    }
                }
            }
        }
        lasthappy=happy;
        //        if (Math.abs(oldV - happyValue) > 0.1) {
        //            Record.append("HAPPY: " + (int) (oldV*10.0) + " to " + (int) (happyValue*10.0) + "\n");
    }

    public void adjustBusy(float newValue, float weight) {
        //        float oldV = busyValue;
        busy += newValue * weight;
        busy /= (1.0f + weight);
        //        if (Math.abs(oldV - busyValue) > 0.1) {
        //            Record.append("BUSY: " + (int) (oldV*10.0) + " to " + (int) (busyValue*10.0) + "\n");
    }
}
