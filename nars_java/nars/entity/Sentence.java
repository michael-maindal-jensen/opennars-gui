/*
 * Sentence.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.entity;

import javolution.text.TextBuilder;
import nars.core.NAR;
import nars.core.Parameters;
import nars.inference.TruthFunctions;
import nars.io.Symbols;
import nars.io.Texts;
import nars.language.Conjunction;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Variables;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * A Sentence is an abstract class, mainly containing a Term, a TruthValue, and
 * a Stamp.
 * <p>
 * It is used as the premises and conclusions of all inference rules.
 */
public class Sentence implements Cloneable {

    /**
     * The content of a Sentence is a Term
     */
    public final Term content;
    
    /**
     * The punctuation also indicates the type of the Sentence: 
     * Judgment, Question, Goal, or Quest.
     * Represented by characters: '.', '?', '!', or '@'
     */
    public final char punctuation;
    
    /**
     * The truth value of Judgment, or desire value of Goal     
     */
    public final TruthValue truth;
    
    /**
     * Partial record of the derivation path
     */
    public final Stamp stamp;

    /**
     * Whether the sentence can be revised
     */
    private boolean revisible;

    /** caches the 'getKey()' result */
    private CharSequence key;

    /**
     * Create a Sentence with the given fields
     *
     * @param content The Term that forms the content of the sentence
     * @param punctuation The punctuation indicating the type of the sentence
     * @param truth The truth value of the sentence, null for question
     * @param stamp The stamp of the sentence indicating its derivation time and
     * base
     */
    public Sentence(final Term content, final char punctuation, final TruthValue truth, final Stamp stamp) {
        this.content = content;        
        this.content.renameVariables();
        
        this.punctuation = punctuation;
        this.truth = truth;
        this.stamp = stamp;
        this.revisible = !((content instanceof Conjunction) && Variables.containVarDep(content.name()));
    }

    /**
     * To check whether two sentences are equal
     *
     * @param that The other sentence
     * @return Whether the two sentences have the same content
     */
    @Override
    public boolean equals(final Object that) {
        if (that instanceof Sentence) {
            final Sentence t = (Sentence) that;
            if (hashCode()!=t.hashCode())
                return false;
            return getKey().equals(t.getKey());
            /*
            return content.equals(t.content) && 
                    punctuation == t.punctuation &&
                    truth.equals(t.truth) &&
                    stamp.equals(t.stamp);
            */
        }
        return false;
    }

    /**
     * To produce the hashcode of a sentence
     *
     * @return A hashcode
     */
    @Override
    public int hashCode() {
        return getKey().hashCode();
        /*
        int hash = 5;
        hash = 67 * hash + (this.content != null ? this.content.hashCode() : 0);
        hash = 67 * hash + (this.punctuation);
        hash = 67 * hash + (this.truth != null ? this.truth.hashCode() : 0);
        hash = 67 * hash + (this.stamp != null ? this.stamp.hashCode() : 0);
        return hash;
        */
    }

    /**
     * Check whether the judgment is equivalent to another one
     * <p>
     * The two may have different keys
     *
     * @param that The other judgment
     * @return Whether the two are equivalent
     */
    public boolean equivalentTo(final Sentence that) {
        //assert content.equals(content) && punctuation == that.punctuation;
        return (truth.equals(that.truth) && stamp.equals(that.stamp));
    }

    /**
     * Clone the Sentence
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        if (truth == null) {
            return new Sentence(content.clone(), punctuation, null, (Stamp) stamp.clone());
        }
        return new Sentence(content.clone(), punctuation, new TruthValue(truth), (Stamp) stamp.clone());
    }

    /** Clone with a different Term */    
    public Sentence clone(Term t) {
        if (truth == null) {
            return new Sentence(t, punctuation, null, (Stamp) stamp.clone());
        }
        return new Sentence(t, punctuation, new TruthValue(truth), (Stamp) stamp.clone());
    }
    
    /**
      * project a judgment to a difference occurrence time
      *
      * @param targetTime The time to be projected into
      * @param currentTime The current time as a reference
      * @return The projected belief
      */    
    public Sentence projection(long targetTime, long currentTime) {
        TruthValue newTruth = new TruthValue(truth);
        boolean eternalizing = false;
        if (stamp.getOccurrenceTime() != Stamp.ETERNAL) {
            newTruth = TruthFunctions.eternalization(truth);
            eternalizing = true;
            if (targetTime != Stamp.ETERNAL) {
                long occurrenceTime = stamp.getOccurrenceTime();
                float factor = TruthFunctions.temporalProjection(occurrenceTime, targetTime, currentTime);
                float projectedConfidence = factor * truth.getConfidence();
                if (projectedConfidence > newTruth.getConfidence()) {
                    newTruth = new TruthValue(truth.getFrequency(), projectedConfidence);
                    eternalizing = false;
                }
            }
        }
        Stamp newStamp = (Stamp) stamp.clone();
        if (eternalizing) {
            newStamp.setOccurrenceTime(Stamp.ETERNAL);
        }
        
        Sentence newSentence = new Sentence((Term) content.clone(), punctuation, newTruth, newStamp);
        return newSentence;
    }




    /**
     * Clone the content of the sentence
     *
     * @return A clone of the content Term
     */
    public Term cloneContent() {
        return content.clone();
    }



    /**
     * Recognize a Judgment
     *
     * @return Whether the object is a Judgment
     */
    public boolean isJudgment() {
        return (punctuation == Symbols.JUDGMENT_MARK);
    }

    /**
     * Recognize a Question
     *
     * @return Whether the object is a Question
     */
    public boolean isQuestion() {
        return (punctuation == Symbols.QUESTION_MARK);
    }

    public boolean isGoal() {
        return (punctuation == Symbols.GOAL_MARK);
    }
 
    public boolean isQuest() {
        return (punctuation == Symbols.QUEST_MARK);
    }    
    
    public boolean containQueryVar() {
        return Variables.containVarQuery(content.name());
    }

    public boolean getRevisible() {
        return revisible;
    }

    public void setRevisible(final boolean b) {
        revisible = b;
    }

    public int getTemporalOrder() {
        return content.getTemporalOrder();
    }
    
    public long getOccurenceTime() {
        return stamp.getOccurrenceTime();
    }
    
    public Operator getOperator() {
        if (content instanceof Operation) {
             return (Operator) ((Statement) content).getPredicate();
        } else {
             return null;
        }
    }    
    
    /**
     * Get a String representation of the sentence
     *
     * @return The String
     */
    @Override
    public String toString() {
        return getKey().toString();
    }

 
    /**
     * Get a String representation of the sentence for key of Task and TaskLink
     *
     * @return The String
     */
    public CharSequence getKey() {
        //key must be invalidated if content or truth change
        if (key == null) {
            final CharSequence contentName = content.name();
            
            final String occurrenceTimeString = ((punctuation == Symbols.JUDGMENT_MARK) || (punctuation == Symbols.QUESTION_MARK)) ? stamp.getOccurrenceTimeString() : "";
            
            final CharSequence truthString = truth != null ? truth.name() : null;

            int stringLength = 0; //contentToString.length() + 1 + 1/* + stampString.baseLength()*/;
            if (truth != null) {
                stringLength += occurrenceTimeString.length() + truthString.length();
            }

            //suffix = [punctuation][ ][truthString][ ][occurenceTimeString]
            final TextBuilder suffix = new TextBuilder(stringLength).append(punctuation);

            if (truth != null) {
                suffix.append(' ').append(truthString);
            }
            if (occurrenceTimeString.length() > 0) {
                suffix.append(' ').append(occurrenceTimeString);
            }

            key = Texts.yarn(Parameters.ROPE_TERMLINK_TERM_SIZE_THRESHOLD, 
                    contentName.toString(), 
                    suffix.toString());
            //key = new FlatCharArrayRope(StringUtil.getCharArray(k));

        }
        return key;
    }

    /**
     * Get a String representation of the sentence for display purpose
     *
     * @return The String
     */
    public CharSequence toString(NAR nar, boolean showStamp) {
    
        CharSequence contentName = content.name();
        
        final long t = nar.memory.getTime();

        final String tenseString = ((punctuation == Symbols.JUDGMENT_MARK) || (punctuation == Symbols.QUESTION_MARK)) ? stamp.getTense(t) : "";
        final String truthString = (truth != null) ? truth.toStringBrief() : null;
 
        CharSequence stampString = showStamp ? stamp.name() : null;
        
        int stringLength = contentName.length() + tenseString.length() + 1 + 1;
                
        if (truth != null) {
            stringLength += truthString.length();
        }
        if (showStamp) {
            stringLength += stampString.length();
        }
        
        final TextBuilder buffer = new TextBuilder(stringLength)
                .append(contentName)
                .append(contentName)
                .append(punctuation);
        
        if (tenseString.length() > 0)
            buffer.append(' ').append(tenseString);
        
        if (truth != null)
            buffer.append(' ').append(truthString);
        
        if (showStamp)
            buffer.append(' ').append(stampString);
        
        return buffer;
    }
    
   
    /**
     * Get the truth value (or desire value) of the sentence
     *
     * @return Truth value, null for question
     */
    public void discountConfidence() {
        truth.setConfidence(truth.getConfidence() * Parameters.DISCOUNT_RATE).setAnalytic(false);
    }



}
