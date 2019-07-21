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
package org.opennars.gui.output;

import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.opennars.main.Nar;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import org.opennars.gui.output.graph.TermSyntaxVis;
import org.opennars.language.Term;

public class SentenceTablePanel extends TablePanel {

    private final JButton syntaxGraphButton;
    private PCanvas syntaxPanel=null;

    public SentenceTablePanel(Nar nar) {
        super(nar);

        data = newModel();

        table.setModel(data);
        table.setAutoCreateRowSorter(true);
        table.validate();
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                syntaxGraphButton.setEnabled(table.getSelectedRowCount() > 0);
            }
        });
        table.getColumn("Type").setMaxWidth(48);
        table.getColumn("Frequency").setMaxWidth(64);
        table.getColumn("Confidence").setMaxWidth(64);
        table.getColumn("Priority").setMaxWidth(64);
        table.getColumn("Complexity").setMaxWidth(64);
        table.getColumn("Time").setMaxWidth(72);
        
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        {
            syntaxGraphButton = new JButton("Graph");
            syntaxGraphButton.setEnabled(false);
            syntaxGraphButton.setBackground(Color.DARK_GRAY);
            syntaxGraphButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newSelectedGraphPanel();
                }
            });
            menu.add(syntaxGraphButton);

            JButton clearButton = new JButton("Clear");
            clearButton.setBackground(Color.DARK_GRAY);
            clearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    data = newModel();
                    table.setModel(data);
                }
            });
            menu.add(clearButton);

        }
        add(menu, BorderLayout.SOUTH);
    }

    public DefaultTableModel newModel() {
        DefaultTableModel data = new DefaultTableModel();
        data.addColumn("Time");
        data.addColumn("Sentence");
        data.addColumn("Type");
        data.addColumn("Frequency");
        data.addColumn("Confidence");
        data.addColumn("Complexity");
        data.addColumn("Priority");
        data.addColumn("ParentTask");
        data.fireTableStructureChanged();
        return data;
    }

    public void newSelectedGraphPanel() {
        Term[] sel=new Term[table.getSelectedRows().length];
        int k=0;
        for(int i: table.getSelectedRows()) {
            Sentence w=(Sentence) table.getValueAt(i, 1);
            sel[k]=w.term;
            k++;
        }
        TermSyntaxVis tt = new TermSyntaxVis(this.nar, sel);
        syntaxPanel = new PCanvas(tt);
        syntaxPanel.setZoom(10f);
        NWindow w = new NWindow("", syntaxPanel);
        w.setSize(400, 400);
        w.setVisible(true);
//        ProcessingGraphPanel2 pgp = new ProcessingGraphPanel2(getSelectedRows(1)) {
//
//            @Override
//            public DirectedMultigraph getGraph() {
//
//                DefaultGraphizer graphizer = new DefaultGraphizer(true, true, true, true, 0, false, false) {
//
//                    protected void addSentence(NARGraph g, Sentence s) {
//                        Term t = s.content;
//                        addTerm(g, t);
//                        //g.addEdge(s, s.content, new NARGraph.SentenceContent());
//
//                        if (t instanceof CompoundTerm) {
//                            CompoundTerm ct = ((CompoundTerm) t);
//                            Set<Term> contained = ct.getContainedTerms();
//
//                            for (Term x : contained) {
//                                addTerm(g, x);
//                                if (ct.containsTerm(x)) {
//                                    g.addEdge(x, t, new NARGraph.TermContent());
//                                }
//
//                                for (Term y : contained) {
//                                    addTerm(g, y);
//
//                                    if (x != y) {
//                                        if (x.containsTerm(y)) {
//                                            g.addEdge(y, x, new NARGraph.TermContent());
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onTime(NARGraph g, long time) {
//                        super.onTime(g, time);
//
//                        for (Object o : getItems()) {
//
//                            if (o instanceof Task) {
//                                g.addVertex(o);
//                                addSentence(g, ((Task) o).sentence);
//                            } else if (o instanceof Sentence) {
//                                g.addVertex(o);
//                                addSentence(g, (Sentence) o);
//                            }
//                        }
//                        //add sentences
//                    }
//
//                };
//
//                app.updating = true;
//
//                graphizer.setShowSyntax(showSyntax);
//
//                NARGraph g = new NARGraph();
//                g.add(nar, newSelectedGraphFilter(), graphizer);
//                return g;
//            }
//
//            @Override
//            public int edgeColor(Object edge) {
//                return NARSwing.getColor(edge.toString(), 0.5f, 0.5f).getRGB();
//            }
//
//            @Override
//            public float edgeWeight(Object edge) {
//                return 10;
//            }
//
//            @Override
//            public int vertexColor(Object vertex) {
//                return NARSwing.getColor(vertex.toString(), 0.5f, 0.5f).getRGB();
//            }
//
//        };
//        NWindow w = new NWindow("", pgp);
//        w.setSize(400, 400);
//        w.setVisible(true);
    }

    @Override
    public void output(Class c, Object o) {
        if (o instanceof Task) {
            Task t = (Task) o;
            float priority = t.getPriority();

            Sentence s = t.sentence;

            float freq = -1;
            float conf = -1;
            TruthValue truth = s.truth;
            if (truth != null) {
                freq = truth.getFrequency();
                conf = (float)truth.getConfidence();
            }

            Task pt = null; //t.getParentTask();
            String parentTask = (pt != null) ? pt.toStringExternal() : "";

            //TODO use table sort instead of formatting numbers with leading '0's
            data.addRow(new Object[]{
                String.format("%08d", nar.time()),
                s,
                s.punctuation,
                freq == -1 ? "" : freq,
                conf == -1 ? "" : conf,
                String.format("%03d", s.term.getComplexity()),
                priority,
                parentTask
            });
        }
    }

    

}
