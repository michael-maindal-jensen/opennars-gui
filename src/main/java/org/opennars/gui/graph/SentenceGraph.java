/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.gui.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.storage.Memory;
import org.opennars.entity.Concept;
import org.opennars.entity.Item;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.language.CompoundTerm;
import org.opennars.LockedValueTypes.PortableDouble;
import org.opennars.language.Statement;
import org.opennars.language.Term;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;



abstract public class SentenceGraph<E> extends DirectedMultigraph<Term, E> implements EventObserver {
    public final Memory memory;

    public static class GraphChange { }
    
    private boolean needInitialConcepts;
    private boolean started;
    
    public final Map<Sentence, List<E>> components = new HashMap();
    
    public final EventEmitter event = new EventEmitter( GraphChange.class );
    PortableDouble minConceptPri;
            
    public SentenceGraph(Memory memory, PortableDouble minConceptPri) {
        super(/*null*/new EdgeFactory() {

            @Override public Object createEdge(Object v, Object v1) {
                return null;
            }
            
        });
        this.minConceptPri = minConceptPri;
        this.memory = memory;
        
        reset();
        
        start();
        
    }
    
    private void setEvents(boolean n) {
        if (memory!=null) {
            memory.event.set(this, n, 
                    Events.CyclesEnd.class, 
                    Events.ConceptForget.class, 
                    Events.ConceptBeliefAdd.class, 
                    Events.ConceptBeliefRemove.class, 
                    Events.ConceptGoalAdd.class, 
                    Events.ConceptGoalRemove.class, 
                    Events.ResetEnd.class
                    );
        }
    }
    
    public void start() {
        if (started) return;        
        started = true;
        setEvents(true);        
    }
    
    public void stop() {
        if (!started) return;
        started = false;
        setEvents(false);
    }

    
    @Override
    public void event(final Class event, final Object[] a) {
//        if (event!=FrameEnd.class)
//            System.out.println(event + " " + Arrays.toString(a));
        
        if (event == Events.ConceptForget.class) {
            //remove all associated beliefs
            Concept c = (Concept)a[0];
            
            //create a clone of the list for thread safety
            for (Task b : new ArrayList<Task>(c.beliefs)) {
                remove(b.sentence);
            }            
        }
        else if (event == Events.ConceptBeliefAdd.class) {
            Concept c = (Concept)a[0];
            Sentence s = ((Task)a[1]).sentence;
            if(c.getPriority() > minConceptPri.get()) {
                add(s, c);
            }
        }
        else if (event == Events.ConceptBeliefRemove.class) {
            Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            remove(s);
        }
        /*else if (event == Events.ConceptGoalAdd.class) {
            Concept c = (Concept)a[0];
            Sentence s = ((Task)a[1]).sentence;
            add(s, c);
        }
        else if (event == Events.ConceptGoalRemove.class) {
            Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            remove(s);
        }*/
        else if (event == Events.CyclesEnd.class) {
            if (needInitialConcepts)
                getInitialConcepts();
        }
        else if (event == Events.ResetEnd.class) {
            reset();
        }
    }    
    

    
        
    protected boolean remove(Sentence s) {
        List<E> componentList = components.get(s);
        if (componentList!=null) {
            for (E e : componentList) {
                if (!containsEdge(e))
                    continue;
                Term source = getEdgeSource(e);
                Term target = getEdgeTarget(e);
                removeEdge(e);
                ensureTermConnected(source);
                ensureTermConnected(target);
            }
            componentList.clear();
            components.remove(s);        
            return true;
        }
        return false;
    }
    
    public void reset() {
        try {
            this.removeAllEdges( new ArrayList(edgeSet()) );
        }
        catch (Exception e) {
            System.err.println(e);
        }
        
        try {
            this.removeAllVertices( new ArrayList(vertexSet()) );
        }
        catch (Exception e) {
            System.err.println(e);
        }
        
        if (!edgeSet().isEmpty()) {
            System.err.println(this + " edges not empty after reset()");
            System.exit(1);
        }
        if (!vertexSet().isEmpty()) {
            System.err.println(this + " vertices not empty after reset()");
            System.exit(1);
        }
            
        needInitialConcepts = true;
    }
    
    private void getInitialConcepts() {
        needInitialConcepts = false;

        try {
            for (final Concept c : memory) {
                for (final Task ts : c.beliefs) {                
                    add(ts.sentence, c);
                }
            }        
        }
        catch (NoSuchElementException e) { }
    }
    
    protected final void ensureTermConnected(final Term t) {
        if (inDegreeOf(t)+outDegreeOf(t) == 0)  removeVertex(t);        
    }
    
        
    abstract public boolean allow(Sentence s);
    
    abstract public boolean allow(CompoundTerm st);    
    
    public boolean remove(final E s) {
        if (!containsEdge(s))
            return false;
        
        Term from = getEdgeSource(s);
        Term to = getEdgeTarget(s);
        
        
        boolean r = removeEdge(s);
        
        
        ensureTermConnected(from);
        ensureTermConnected(to);

        if (r)
            event.emit(GraphChange.class, null, s);
        return true;
    }
    
   
    protected void addComponents(final Sentence parentSentence, final E edge) {
        List<E> componentList = components.get(parentSentence);
        if (componentList == null) {
            componentList = new ArrayList(1);
            components.put(parentSentence, componentList);
        }
        componentList.add(edge);        
    }
    
    public boolean add(final Sentence s, final Item c) {         

        if (!allow(s))
            return false;               
        
        if (s.term instanceof CompoundTerm) {
            CompoundTerm cs = (CompoundTerm)s.term;
        
            if (cs instanceof Statement) {
                
                
                Statement st = (Statement)cs;
                if (allow(st)) {                                                    
                    
                    if (add(s, st, c)) {
                        event.emit(GraphChange.class, st, null);
                        return true;
                    }
                }
            }
                
        }        
        
        return false;
    }    
    
    /** default behavior, may override in subclass */
    abstract public boolean add(final Sentence s, final CompoundTerm ct, final Item c);

    
}
