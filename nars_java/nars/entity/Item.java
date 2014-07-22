/*
 * Item.java
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

/**
 * An item is an object that can be put into a Bag,
 * to participate in the resource competition of the system.
 * <p>
 * It has a key and a budget. Cannot be cloned
 */
public abstract class Item {

    /** The key of the Item, unique in a Bag */
    protected String key;
    /** The budget of the Item, consisting of 3 numbers */
    protected final BudgetValue budget;

    /**
     * The default constructor
     */
    protected Item() {
        budget = null;
    }

    /**
     * Constructor with default budget
     * @param key The key value
     */
    protected Item(final String key) {
        this.key = key;
        this.budget = new BudgetValue();
     }

    /**
     * Constructor with initial budget
     * @param key The key value
     * @param budget The initial budget
     */
    protected Item(final String key, final BudgetValue budget) {
        this.key = key;
        this.budget = new BudgetValue(budget);  // clone, not assignment
    }


    /**
     * Get the current key
     * @return Current key value
     */
    public String getKey() {
        return key;
    }

    /**
     * Get BudgetValue
     * @return Current BudgetValue
     */
    public BudgetValue getBudget() {
        return budget;
    }

    /**
     * Get priority value
     * @return Current priority value
     */
     public float getPriority() {
        return budget.getPriority();
    }

    /**
     * Set priority value
     * @param v Set a new priority value
     */
    public void setPriority(final float v) {
        budget.setPriority(v);
    }

    /**
     * Increase priority value
     * @param v The amount of increase
     */
    public void incPriority(final float v) {
        budget.incPriority(v);
    }

    /**
     * Decrease priority value
     * @param v The amount of decrease
     */
    public void decPriority(final float v) {
        budget.decPriority(v);
    }

    /**
     * Get durability value
     * @return Current durability value
     */
    public float getDurability() {
        return budget.getDurability();
    }

    /**
     * Set durability value
     * @param v The new durability value
     */
    public void setDurability(final float v) {
        budget.setDurability(v);
    }

    /**
     * Increase durability value
     * @param v The amount of increase
     */
    public void incDurability(final float v) {
        budget.incDurability(v);
    }

    /**
     * Decrease durability value
     * @param v The amount of decrease
     */
    public void decDurability(final float v) {
        budget.decDurability(v);
    }

    /**
     * Get quality value
     * @return The quality value
     */
    public float getQuality() {
        return budget.getQuality();
    }

    /**
     * Set quality value
     * @param v The new quality value
     */
    public void setQuality(final float v) {
        budget.setQuality(v);
    }

    /**
     * Merge with another Item with identical key
     * @param that The Item to be merged
     */
    public void merge(final Item that) {
        budget.merge(that.getBudget());
    }

    /**
     * Return a String representation of the Item
     * @return The String representation of the full content
     */
    @Override
    public String toString() {        
        //return budget + " " + key ;
        String budgetStr = budget.toString();
        return new StringBuilder(budgetStr.length()+key.length()+1).append(budgetStr).append(' ').append(key).toString();
    }

    /**
     * Return a String representation of the Item after simplification
     * @return A simplified String representation of the content
     */
    public String toStringBrief() {        
        //return budget.toStringBrief() + " " + key ;
        final String briefBudget = budget.toStringBrief();
        return new StringBuilder(briefBudget.length()+key.length()+1).append(briefBudget).append(' ').append(key).toString();
    }
    
    public String toStringLong() {
    	return toString();
    }

}
