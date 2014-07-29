/*
 * Statement.java
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

import java.util.ArrayList;
import nars.inference.TemporalRules;

import nars.io.Symbols;
import nars.io.Symbols.Operator;
import nars.storage.Memory;

/**
 * A statement is a compound term, consisting of a subject, a predicate, and a
 * relation symbol in between. It can be of either first-order or higher-order.
 */
public abstract class Statement extends CompoundTerm {
    private StringBuilder nameBuilder;

    
    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    protected Statement(final ArrayList<Term> arg) {
        super(arg);
    }

    

    /**
     * Constructor with full values, called by clone
     *
     * @param n The nameStr of the term
     * @param cs Component list
     * @param con Constant indicator
     * @param i Syntactic complexity of the compound
     */
    protected Statement(final String n, final ArrayList<Term> cs, final boolean con, final short i) {
        super(n, cs, con, i);
    }
    
    
    protected Statement(final String n, final ArrayList<Term> cs, final boolean con, final boolean hasVar, final short i, int nameHash) {
        super(n, cs, con, hasVar, i, nameHash);
    }


    /**
     * Make a Statement from String, called by StringParser
     *
     * @param o The relation String
     * @param subject The first component
     * @param predicate The second component
     * @param memory Reference to the memory
     * @return The Statement built
     */
    public static Statement make(final Operator o, final Term subject, final Term predicate, final Memory memory) {
        if (invalidStatement(subject, predicate)) {
            return null;
        }
        
        switch (o) {
            case INHERITANCE:
                return Inheritance.make(subject, predicate, memory);
            case SIMILARITY:
                return Similarity.make(subject, predicate, memory);
            case INSTANCE:
                return Instance.make(subject, predicate, memory);
            case PROPERTY:
                return Property.make(subject, predicate, memory);
            case INSTANCE_PROPERTY:
                return InstanceProperty.make(subject, predicate, memory);
            case IMPLICATION:
                return Implication.make(subject, predicate, memory);
            case IMPLICATION_AFTER:
                return Implication.make(subject, predicate, TemporalRules.ORDER_FORWARD, memory);
            case IMPLICATION_BEFORE:
                return Implication.make(subject, predicate, TemporalRules.ORDER_BACKWARD, memory);
            case IMPLICATION_WHEN:
                return Implication.make(subject, predicate, TemporalRules.ORDER_CONCURRENT, memory);
            case EQUIVALENCE:
                return Equivalence.make(subject, predicate, memory);
            case EQUIVALENCE_AFTER:
                return Equivalence.make(subject, predicate, TemporalRules.ORDER_FORWARD, memory);
            case EQUIVALENCE_WHEN:
                return Equivalence.make(subject, predicate, TemporalRules.ORDER_CONCURRENT, memory);            
        }
        
        return null;
    }

    /**
     * Make a Statement from given components, called by the rules
     *
     * @param order The temporal order of the statement
     * @return The Statement built
     * @param subj The first component
     * @param pred The second component
     * @param statement A sample statement providing the class type
     * @param memory Reference to the memory
     */
//    public static Statement make(final Statement statement, final Term subj, final Term pred, final Memory memory) {
//        return make(statement, subj, pred, TemporalRules.ORDER_NONE, memory);
//    }
    
    public static Statement make(final Statement statement, final Term subj, final Term pred, int order, final Memory memory) {

        if (statement instanceof Inheritance) {
            return Inheritance.make(subj, pred, memory);
        }
        if (statement instanceof Similarity) {
            return Similarity.make(subj, pred, memory);
        }
        if (statement instanceof Implication) {
            return Implication.make(subj, pred, order, memory);
        }
        if (statement instanceof Equivalence) {
            return Equivalence.make(subj, pred, order, memory);
        }
        return null;
    }

    /**
     * Make a symmetric Statement from given components and temporal
     * information, called by the rules
     *
     * @param statement A sample asymmetric statement providing the class type
     * @param subj The first component
     * @param pred The second component
     * @param order The temporal order
     * @param memory Reference to the memory
     * @return The Statement built
     */
    public static Statement makeSym(final Statement statement, final Term subj, final Term pred, final int order, final Memory memory) {
        if (statement instanceof Inheritance) {
            return Similarity.make(subj, pred, memory);
        }
        if (statement instanceof Implication) {
            return Equivalence.make(subj, pred, order, memory);
        }
        return null;
    }



    /**
     * Override the default in making the nameStr of the current term from
     * existing fields
     *
     * @return the nameStr of the term
     */
    @Override
    protected String makeName() {
        if (nameBuilder==null)
            nameBuilder = new StringBuilder();
        
        return makeStatementName(getSubject(), operator(), getPredicate(), nameBuilder);
    }

    /**
     * Default method to make the nameStr of an image term from given fields
     *
     * @param subject The first component
     * @param predicate The second component
     * @param relation The relation operator
     * @return The nameStr of the term
     */
    protected static String makeStatementName(final Term subject, final Operator relation, final Term predicate, StringBuilder nameBuilder) {
        final String subjectName = subject.getName();
        final String predicateName = predicate.getName();
        int length = subjectName.length() + predicateName.length() + relation.toString().length() + 4;
        
        if (nameBuilder == null) {
            nameBuilder = new StringBuilder();
        }
        nameBuilder.setLength(0);
        nameBuilder.ensureCapacity(length);
        
        return nameBuilder
            .append(Symbols.STATEMENT_OPENER)
            .append(subjectName)
            .append(' ').append(relation).append(' ')
            .append(predicateName)
            .append(Symbols.STATEMENT_CLOSER)
            .toString();
    }
    
    protected static String makeStatementName(final Term subject, final Operator relation, final Term predicate) {
        return makeStatementName(subject, relation, predicate, null);
    }

    /**
     * Check the validity of a potential Statement. [To be refined]
     * <p>
     * @param subject The first component
     * @param predicate The second component
     * @return Whether The Statement is invalid
     */
    public static boolean invalidStatement(final Term subject, final Term predicate) {
        if (subject.equals(predicate)) {
            return true;
        }
        if (invalidReflexive(subject, predicate)) {
            return true;
        }
        if (invalidReflexive(predicate, subject)) {
            return true;
        }
        if ((subject instanceof Statement) && (predicate instanceof Statement)) {
            final Statement s1 = (Statement) subject;
            final Statement s2 = (Statement) predicate;
            final Term t11 = s1.getSubject();
            final Term t12 = s1.getPredicate();
            final Term t21 = s2.getSubject();
            final Term t22 = s2.getPredicate();
            if (t11.equals(t22) && t12.equals(t21)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if one term is identical to or included in another one, except in a
     * reflexive relation
     * <p>
     * @param t1 The first term
     * @param t2 The second term
     * @return Whether they cannot be related in a statement
     */
    private static boolean invalidReflexive(final Term t1, final Term t2) {
        if (!(t1 instanceof CompoundTerm)) {
            return false;
        }
        final CompoundTerm com = (CompoundTerm) t1;
        if ((com instanceof ImageExt) || (com instanceof ImageInt)) {
            return false;
        }
        return com.containComponent(t2);
    }

    
    public static boolean invalidPair(final String s1, final String s2) {
        if (Variable.containVarIndep(s1) && !Variable.containVarIndep(s2)) {
            return true;
        } else if (!Variable.containVarIndep(s1) && Variable.containVarIndep(s2)) {
            return true;
        }
        return false;
    }

    /**
     * Check the validity of a potential Statement. [To be refined]
     * <p>
     * Minimum requirement: the two terms cannot be the same, or containing each
     * other as component
     *
     * @return Whether The Statement is invalid
     */
    public boolean invalid() {
        return invalidStatement(getSubject(), getPredicate());
    }
    
 
    /**
     * Return the first component of the statement
     *
     * @return The first component
     */
    public Term getSubject() {
        return components.get(0);
    }

    /**
     * Return the second component of the statement
     *
     * @return The second component
     */
    public Term getPredicate() {
        return components.get(1);
    }
}
