/*
 * Bag.java
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
package nars.storage;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;

import nars.entity.Item;
import nars.inference.BudgetFunctions;
import nars.core.Parameters;

/**
 * A Bag is a storage with a constant capacity and maintains an internal
 * priority distribution for retrieval.
 * <p>
 * Each entity in a bag must extend Item, which has a BudgetValue and a key.
 * <p>
 * A name table is used to merge duplicate items that have the same key.
 * <p>
 * The bag space is divided by a threshold, above which is mainly time
 * management, and below, space management. Differences: (1) level selection vs.
 * item selection, (2) decay rate
 *
 * @param <E> The type of the Item in the Bag
 */
public abstract class Bag<E extends Item>  {

    /**
     * priority levels
     */
    public final int levels;
    /**
     * firing threshold
     */
    public final int THRESHOLD;
    /**
     * relative threshold, only calculate once
     */
    private float RELATIVE_THRESHOLD;
    /**
     * hashtable load factor
     */
    public static final float LOAD_FACTOR = Parameters.LOAD_FACTOR;       //
    

    
    /**
     * shared DISTRIBUTOR that produce the probability distribution
     */
    private final int[] DISTRIBUTOR;
    
    
    /**
     * mapping from key to item
     */
    public final HashMap<String, E> nameTable;
    /**
     * array of lists of items, for items on different level
     */
    public final Deque<E>[] itemTable;
    
    //this is a cache holding whether itemTable[i] is empty. 
    //it avoids needing to call itemTable.isEmpty() which may improve performance
    //it is maintained each time an item is added or removed from one of the itemTable ArrayLists
    protected final boolean[] itemTableEmpty;
    
    /**
     * defined in different bags
     */
    final int capacity;
    /**
     * current sum of occupied level
     */
    private int mass;
    /**
     * index to get next level, kept in individual objects
     */
    private int levelIndex;
    /**
     * current take out level
     */
    private int currentLevel;
    /**
     * maximum number of items to be taken out at current level
     */
    private int currentCounter;

    
    private BagObserver<E> bagObserver = null;
    
    /**
     * The display level; initialized at lowest
     */
    private int showLevel;

    protected Bag(int levels, int capacity) {
        this.levels = levels;
        THRESHOLD = showLevel = (int)(Parameters.BAG_THRESHOLD * levels);
        RELATIVE_THRESHOLD = Parameters.BAG_THRESHOLD;
        this.capacity = capacity;
        nameTable = new HashMap<>((int) (capacity / LOAD_FACTOR), LOAD_FACTOR);
        itemTableEmpty = new boolean[this.levels];
        itemTable = new Deque[this.levels];
        DISTRIBUTOR = Distributor.get(this.levels).order;
        clear();
        //showing = false;        
    }
    

    public void clear() {
        for (int i = 0; i < levels; i++) {
            itemTableEmpty[i] = true;
            if (itemTable[i]!=null)
                itemTable[i].clear();
        }
        nameTable.clear();
        currentLevel = levels - 1;
        levelIndex = capacity % levels; // so that different bags start at different point
        mass = 0;
        currentCounter = 0;
    }



    /**
     * Get the item decay rate, which differs in difference subclass, and can be
     * changed in run time by the user, so not a constant.
     *
     * @return The number of times for a decay factor to be fully applied
     */
    protected abstract int forgetRate();

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    public int size() {
        return nameTable.size();
    }

    /**
     * Get the average priority of Items
     *
     * @return The average priority of Items in the bag
     */
    public float getAveragePriority() {
        if (size() == 0) {
            return 0.01f;
        }
        float f = (float) mass / (size() * levels);
        if (f > 1) {
            return 1.0f;
        }
        return f;
    }

    /**
     * Check if an item is in the bag
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    public boolean contains(final E it) {
        return nameTable.containsValue(it);
    }

    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    public E get(final String key) {
        return nameTable.get(key);
    }

    /**
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @return Whether the new Item is added into the Bag
     */
    public boolean putIn(final E newItem) {
        final String newKey = newItem.getKey();
                
        
        final E oldItem = nameTable.put(newKey, newItem);
        if (oldItem != null) {                  // merge duplications
            outOfBase(oldItem);
            newItem.merge(oldItem);
        }
        final E overflowItem = intoBase(newItem);  // put the (new or merged) item into itemTable
        if (overflowItem != null) {             // remove overflow
            final String overflowKey = overflowItem.getKey();
            nameTable.remove(overflowKey);
            return (overflowItem != newItem);
        } else {
            return true;
        }
    }

    /**
     * Put an item back into the itemTable
     * <p>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @return Whether the new Item is added into the Bag
     */
    public boolean putBack(final E oldItem) {
        BudgetFunctions.forget(oldItem.getBudget(), forgetRate(), RELATIVE_THRESHOLD);
        return putIn(oldItem);
    }

    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item
     */
    public E takeOut() {
        if (nameTable.isEmpty()) { // empty bag
            return null;
        }
        if (itemTableEmpty[currentLevel] || (currentCounter == 0)) { // done with the current level
            
            // look for a non-empty level
            do {
                currentLevel = DISTRIBUTOR[levelIndex];
                levelIndex = (levelIndex + 1) % DISTRIBUTOR.length;
            } while (itemTableEmpty[currentLevel]);
            
            if (currentLevel < THRESHOLD) { // for dormant levels, take one item
                currentCounter = 1;
            } else {                  // for active levels, take all current items
                currentCounter = getLevelSize(currentLevel);
            }
        }
        final E selected = takeOutFirst(currentLevel); // take out the first item in the level
        currentCounter--;
        nameTable.remove(selected.getKey());
        refresh();
        return selected;
    }

    public int getLevelSize(final int level) {        
        return (itemTableEmpty[level]) ? 0 : itemTable[level].size();
    }
    
    /**
     * Pick an item by key, then remove it from the bag
     *
     * @param key The given key
     * @return The Item with the key
     */
    public E pickOut(final String key) {
        final E picked = nameTable.get(key);
        if (picked != null) {
            outOfBase(picked);
            nameTable.remove(key);
        }
        return picked;
    }

    /**
     * Check whether a level is empty
     *
     * @param n The level index
     * @return Whether that level is empty
     */
    public boolean emptyLevel(final int n) {
        return itemTableEmpty[n];
    }

    /**
     * Decide the put-in level according to priority
     *
     * @param item The Item to put in
     * @return The put-in level
     */
    private int getLevel(final E item) {
        final float fl = item.getPriority() * levels;
        final int level = (int) Math.ceil(fl) - 1;
        return (level < 0) ? 0 : level;     // cannot be -1
    }

    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return The overflow Item
     */
    private E intoBase(E newItem) {
        E oldItem = null;
        int inLevel = getLevel(newItem);
        if (size() > capacity) {      // the bag is full
            int outLevel = 0;
            //while (itemTable[outLevel].isEmpty()) {
            while (itemTableEmpty[outLevel]) {
                outLevel++;
            }
            if (outLevel > inLevel) {           // ignore the item and exit
                return newItem;
            } else {                            // remove an old item in the lowest non-empty level
                oldItem = takeOutFirst(outLevel);
            }        
        }
        ensureLevelExists(inLevel);
        itemTable[inLevel].add(newItem);        // FIFO
        itemTableEmpty[inLevel] = false;
        mass += (inLevel + 1);                  // increase total mass
        refresh();                              // refresh the window
        return oldItem;		// TODO return null is a bad smell
    }

    protected void ensureLevelExists(int level) {
        if (itemTable[level]==null)
            itemTable[level] = newLevel();
    }
    
    protected Deque<E> newLevel() {
        //return new LinkedList<E>();
        return new ArrayDeque<E>(1+capacity/levels);
    }
    
    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    private E takeOutFirst(final int level) {
        final E selected = itemTable[level].removeFirst();
        itemTableEmpty[level] = itemTable[level].isEmpty();
        mass -= (level + 1);
        refresh();
        return selected;
    }

    /**
     * Remove an item from itemTable, then adjust mass
     *
     * @param oldItem The Item to be removed
     */
    protected void outOfBase(final E oldItem) {
        final int level = getLevel(oldItem);
        itemTable[level].remove(oldItem);
        itemTableEmpty[level] = itemTable[level].isEmpty();
        mass -= (level + 1);
        refresh();
    }

    /**
     * To start displaying the Bag in a BagWindow; {@link nars.gui.BagWindow}
     * implements interface {@link BagObserver};
     *
     * @param bagObserver BagObserver to set
     * @param title The title of the window
     */
    public void addBagObserver(BagObserver<E> bagObserver, String title) {
        this.bagObserver = bagObserver;
        bagObserver.post(toString());
        bagObserver.setTitle(title);
        bagObserver.setBag(this);
    }

    /**
     * Resume display
     */
    public void play() {
        if (bagObserver!=null)
            bagObserver.post(toString());
    }

    /**
     * Stop display
     */
    public void stop() {
        if (bagObserver!=null)        
            bagObserver.stop();
    }

    /**
     * Refresh display
     */
    protected void refresh() {
        if (bagObserver!=null)       
            if (bagObserver.isActive()) {
                bagObserver.refresh(toString());
            }
    }

    /**
     * Collect Bag content into a String for display
     */
    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer(" ");
        for (int i = levels-1; i >= showLevel; i--) {
            if (itemTable[i]!=null && !itemTable[i].isEmpty()) {
                buf.append("\n --- Level ").append((i+1)).append(":\n");
                for (final E e : itemTable[i]) {
                    buf.append(e.toStringBrief()).append('\n');
                }
            }
        }
        return buf.toString();
    }

    /**
     * TODO refactor : paste from preceding method
     */
    public String toStringLong() {
        StringBuffer buf = new StringBuffer(" BAG " + getClass().getSimpleName());
        buf.append(" ").append(showSizes());
        for (int i = levels; i >= showLevel; i--) {
            if (!itemTableEmpty[i-1]) {
                buf = buf.append("\n --- LEVEL ").append(i).append(":\n ");
                for (final E e : itemTable[i-1]) {
                    buf = buf.append(e.toStringLong()).append('\n');
                }
                
            }
        }
        buf.append(">>>> end of Bag").append(getClass().getSimpleName());
        return buf.toString();
    }

    /**
     * show item Table Sizes
     */
    public String showSizes() {
        StringBuilder buf = new StringBuilder(" ");
        int l = 0;
        for (Collection<E> items : itemTable) {
            if ((items != null) && (!items.isEmpty())) {
                l++;
                buf.append(items.size()).append(' ');
            }
        }
        return "Levels: " + Integer.toString(l) + ", sizes: " + buf;
    }

    /**
     * set Show Level
     */
    public void setShowLevel(int showLevel) {
        this.showLevel = showLevel;
    }

    public float getMass() {
        return mass;
    }
    
    public float getAverageItemsPerLevel() {
        return ((float)capacity)/((float)levels);
    }
    public float getMaxItemsPerLevel() {
        int max = getLevelSize(0);
        for (int i = 1; i < levels; i++) {
            int s = getLevelSize(i);
            if (s > max) max = s;
        }
        return max;
    }
    public float getMinItemsPerLevel() {
        int min = getLevelSize(0);
        for (int i = 1; i < levels; i++) {
            int s = getLevelSize(i);
            if (s < min) min = s;
        }
        return min;
    }

    public int getCapacity() {
        return capacity;
    }

    public Deque<E> getLevel(final int i) {
        return itemTable[i];
    }
        
}
