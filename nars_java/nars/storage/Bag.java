package nars.storage;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.Item;


public abstract class Bag<E extends Item<K>,K> implements Iterable<E> {
    
    public static final int bin(final float x, final int bins) {
        int i = (int)Math.floor((x + 0.5f/bins) * bins);
        return i;
    }


    public abstract void clear();   

    /**
     * Check if an item is in the bag.  both its key and its value must match the parameter
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    public boolean contains(final E it) {
        E exist = get(it.name());
        if (exist.equals(it))
            return true;
        return false;
    }
    
    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    abstract public E get(final K key);
    
    abstract public Set<K> keySet();

    abstract public int getCapacity();

    abstract public float getMass();

    /**
     * Choose an Item according to distribution policy and take it out of the Bag
     * @return The selected Item, or null if this bag is empty
     */
    abstract public E takeNext();
    

    /** gets the next value without removing changing it or removing it from any index */    
    abstract public E peekNext();
    
    
    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return The overflow Item, or null if nothing displaced
     */
    abstract protected E addItem(final E newItem);

    /**
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @return the item which was removed, which may be the input item if it could not be inserted; or null if nothing needed removed
     */
    public E putIn(E newItem) {
                
        final K newKey = newItem.name();
        
        final E existingItemWithSameKey = take(newKey);
        
        if (existingItemWithSameKey != null) {            
            newItem = (E)existingItemWithSameKey.merge(newItem);
        }
        
        // put the (new or merged) item into itemTable        
        final E overflowItem = addItem(newItem);
        
        
        if (overflowItem!=null) {
            return overflowItem;
        }            
        else {
            return null;
        }
        
    }

    abstract public E take(final K key);

    
    
    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    public abstract int size();
    
    
    public void printAll() {
        Iterator<E> d = iterator();
        while (d.hasNext()) {
            System.out.println("  " + d.next() + "\n" );
        }
    }
    
    abstract public Collection<E> values();

    public abstract float getAveragePriority();

    public float getTotalPriority() {
        int size = size();
        if (size == 0) return 0;
        return getAveragePriority() * size();
    }

    /** iterates all items in (approximately) descending priority */
    @Override public abstract Iterator<E> iterator();
    
    /** allows adjusting forgetting rate in subclasses */    
    public float getForgetCycles(final float baseForgetCycles, final E item) {
        return baseForgetCycles;
    }
    
    
    /**
     * Put an item back into the itemTable
     * <p>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @return the item which was removed, or null if none removed
     */    
    public E putBack(final E oldItem, final float forgetCycles, final Memory m) {
        float relativeThreshold = Parameters.BAG_THRESHOLD;
        m.forget(oldItem, getForgetCycles(forgetCycles, oldItem), relativeThreshold);
        return putIn(oldItem);
    }
    
    
    /** x = takeOut(), then putBack(x)
     *  @forgetCycles forgetting time in cycles
     *  @return the variable that was updated, or null if none was taken out
     */
    public E processNext(final float forgetCycles, final Memory m) {
                
        final E x = takeNext();
        
        if (x!=null) {            
            E r = putBack(x, forgetCycles, m);
            if (r!=null) {
                throw new RuntimeException("Bag.processNext should always be able to re-insert item: " + r);
            }
            return x;
        }
        else {
            return null;
        }
    }
    
    public double[] getPriorityDistribution(double[] x) {
        int bins = x.length;
        double total = 0;
        for (E e : values()) {
            float p = e.budget.getPriority();
            int b = bin(p, bins-1);
            x[b]++;
            total++;
        }
        if (total > 0) {
            for (int i = 0; i < bins; i++)
                x[i] /= total;
        }
        return x;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();// + "(" + size() + "/" + getCapacity() +")";
    }
    
    /** slow, probably want to override in subclasses */
    public float getMinPriority() {
        float min = 1.0f;
        for (Item e : this) {
            float p = e.getPriority();
            if (p < min) min = p;
        }
        return min;            
    }
    
    /** slow, probably want to override in subclasses */
    public float getMaxPriority() {
        float max = 0.0f;
        for (Item e : this) {
            float p = e.getPriority();
            if (p > max) max = p;
        }
        return max;
    }

    
}
