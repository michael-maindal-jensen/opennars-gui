/*
 * IntersectionExt.java
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
package nars.language;

import java.util.Collection;
import java.util.TreeSet;
import nars.io.Symbols.NativeOperator;

/**
 * A compound term whose extension is the intersection of the extensions of its term
 */
public class IntersectionExt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private IntersectionExt(final Term[] arg) {
        super(arg);
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    private IntersectionExt(Term[] cloneTerms, int temporalOrder, boolean constant, boolean containsVar, short complexity, int hashCode) {
        super(cloneTerms, temporalOrder, constant, containsVar, complexity, hashCode);
    }

    /**
     * Clone an object
     * @return A new object, to be casted into a Conjunction
     */
    @Override
    public IntersectionExt clone() {
        return new IntersectionExt(cloneTerms(), getTemporalOrder(), isConstant(), containsVar(), getComplexity(), hashCode());
    }
    
    @Override public boolean validSize(int num) {
        return num>=2;
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param term1 The first compoment
     * @param term2 The first compoment
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Term term1, Term term2) {
        TreeSet<Term> set;
        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
            set = new TreeSet<Term>(((CompoundTerm) term1).cloneTermsList());
            set.addAll(((CompoundTerm) term2).cloneTermsList());        // set union
            return SetInt.make(set);
        }
        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
            set = new TreeSet<Term>(((CompoundTerm) term1).cloneTermsList());
            set.retainAll(((CompoundTerm) term2).cloneTermsList());     // set intersection
            return SetExt.make(set);
        }
        if (term1 instanceof IntersectionExt) {
            set = new TreeSet<Term>(((CompoundTerm) term1).cloneTermsList());
            if (term2 instanceof IntersectionExt) {
                set.addAll(((CompoundTerm) term2).cloneTermsList());
            }               // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
            else {
                set.add(term2.clone());
            }               // (&,(&,P,Q),R) = (&,P,Q,R)
        } else if (term2 instanceof IntersectionExt) {
            set = new TreeSet<Term>(((CompoundTerm) term2).cloneTermsList());
            set.add(term1.clone());    // (&,R,(&,P,Q)) = (&,P,Q,R)
        } else {
            set = new TreeSet<Term>();
            set.add(term1.clone());
            set.add(term2.clone());
        }
        return make(set);
    }

    /**
     * Try to make a new IntersectionExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argList The list of term
     * @param memory Reference to the memory
     */
    public static Term make(Collection<Term> argList) {        
        return make(new TreeSet<Term>(argList)); // sort/merge arguments
    }

    /**
     * Try to make a new compound from a set of term. Called by the public make methods.
     * @param set a set of Term as compoments
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    public static Term make(TreeSet<Term> set) {        
        if (set.size() == 1) {
            // special case: single component
            return set.first();
        }                   
        
        Term[] argument = set.toArray(new Term[set.size()]);
        return new IntersectionExt(argument);
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.INTERSECTION_EXT;
    }

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }
}
