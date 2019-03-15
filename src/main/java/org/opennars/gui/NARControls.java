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
package org.opennars.gui;

import automenta.vivisect.swing.AwesomeButton;
import automenta.vivisect.swing.NSlider;
import automenta.vivisect.swing.NWindow;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.NORTH;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import org.opennars.LockedValueTypes.PortableInteger;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.main.Nar;
import org.opennars.storage.Memory;
import org.opennars.gui.input.TextInputPanel;
import org.opennars.gui.output.PluginPanel;
import org.opennars.gui.output.SentenceTablePanel;
import org.opennars.gui.output.SwingLogPanel;
import org.opennars.gui.output.TaskTree;
import org.opennars.gui.output.graph.NARGraphPanel;
import org.opennars.io.events.OutputHandler;
import org.opennars.io.events.TextOutputHandler;
import org.opennars.io.events.Events.CyclesEnd;
import org.xml.sax.SAXException;

public class NARControls extends JPanel implements ActionListener, EventObserver {
    /**
     * Reference to the reasoner
     */
    public final Nar nar;

    /**
     * Reference to the memory
     */
    private final Memory memory;
    
    /**
     * Reference to the experience writer
     */
    private final TextOutputHandler experienceWriter;


    /**
     * Control buttons
     */
    private JButton stopButton, walkButton;

    /**
     * Whether the experience is saving into a file
     */
    private boolean savingExp = false;

    private NSlider speedSlider;
    private float currentSpeed = 0f;
    private float lastSpeed = 0f;
    private final float defaultSpeed = 0.5f;

    private final int GUIUpdatePeriodMS = 75;
    private NSlider volumeSlider;
    
    private NSlider decisionThresholdSlider;

    private boolean allowFullSpeed = true;
    public final InferenceLogger logger;
    
    private NSlider threadSlider;

    int chartHistoryLength = 128;

    public NARSwing parent;

    /**
     * Constructor
     *
     * @param nar reasoner instance for the controls
     * @param parent parent window in which the object resides in
     */
    public NARControls(final Nar nar, NARSwing parent) {
        super(new BorderLayout());
        
        this.nar = nar;
        memory = nar.memory; 
        this.parent = parent;
        
        experienceWriter = new TextOutputHandler(nar);
        
        logger = new InferenceLogger(nar);
        logger.setActive(false);
        
        JMenuBar menuBar = new JMenuBar();

        JMenu m = new JMenu("Memory");
        addJMenuItem(m, "Reset");
        m.addSeparator();
        addJMenuItem(m, "Load Experience");
        addJMenuItem(m, "Save Experience");

        addJMenuItem(m, "Save Memory");     
        addJMenuItem(m, "Load Memory");

        m.addActionListener(this);
        menuBar.add(m);

        m = new JMenu("Windows");
        {
            
            JMenuItem mv3 = new JMenuItem("+ Input");
            mv3.addActionListener(e -> {
                TextInputPanel inputPanel = new TextInputPanel(nar);
                NWindow inputWindow = new NWindow("Input", inputPanel);
                inputWindow.setSize(800, 200);
                inputWindow.setVisible(true);
            });
            m.add(mv3);
                       
            JMenuItem ml = new JMenuItem("+ Output");
            ml.addActionListener(e -> new NWindow("Output", new SwingLogPanel(NARControls.this)).show(500, 300));
            m.add(ml);
            
            m.addSeparator();


            JMenuItem mv = new JMenuItem("+ Concept Network");
            mv.addActionListener(e -> new NWindow("graphvis", new NARGraphPanel( nar) ).show(800, 800, false));
            m.add(mv);
            
            m.addSeparator();
                        
            JMenuItem tt = new JMenuItem("+ Task Tree");
            tt.addActionListener(e -> new NWindow("Task Tree", new TaskTree(nar)).show(300, 650, false));
            m.add(tt);
            
          //  m.addSeparator();
            
            JMenuItem st = new JMenuItem("+ Sentence Table");
            st.addActionListener(e -> {
                SentenceTablePanel p = new SentenceTablePanel(nar);
                NWindow w = new NWindow("Sentence Table", p);
                w.setSize(500, 300);
                w.setVisible(true);
            });
            m.add(st);
            
            m.addSeparator();
            
            JMenuItem gml = new JMenuItem("+ Concept Forgetting Log");
            gml.addActionListener(e -> new NWindow("Forgot", new SwingLogPanel(NARControls.this,
                    Events.ConceptForget.class
                    //, Events.TaskRemove.class, Events.TermLinkRemove.class, Events.TaskLinkRemove.class)
            ))
            .show(500, 300));
            m.add(gml);
            
            JMenuItem gml2 = new JMenuItem("+ Task Forgetting Log");
            gml2.addActionListener(e -> new NWindow("Forgot", new SwingLogPanel(NARControls.this,
                    Events.TaskRemove.class
                    //, Events.TaskRemove.class, Events.TermLinkRemove.class, Events.TaskLinkRemove.class)
            ))
            .show(500, 300));
            m.add(gml2);

        }
        menuBar.add(m);

        m = new JMenu("Help");
        addJMenuItem(m, "About NARS");
        m.addActionListener(this);
        menuBar.add(m);

        
        JPanel top = new JPanel(new BorderLayout());
        
        top.add(menuBar, BorderLayout.NORTH);


        JComponent jp = newParameterPanel();
        top.add(jp, BorderLayout.CENTER);


        add(top, NORTH);
        
        
        init();
        volumeSlider.setValue(nar.narParameters.VOLUME);
        decisionThresholdSlider.setValue(nar.narParameters.DECISION_THRESHOLD);
        threadSlider.setValue(nar.narParameters.THREADS_AMOUNT);
        
    }

    /**
     * @param m
     * @param item
     */
    private JMenuItem addJMenuItem(JMenu m, String item) {
        JMenuItem menuItem = new JMenuItem(item);
        m.add(menuItem);
        menuItem.addActionListener(this);
        return menuItem;
    }

    public void showExperienceFileThreadingInfo() {
        if(nar.narParameters.THREADS_AMOUNT > 1) { 
            JOptionPane.showMessageDialog(null, "Using experience files in multi-threaded mode doesn't lead to reproducible outputs, as determinism is lost by the thread scheduling, making them effectively input files!", "Note: ", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    /**
     * Open an addInput experience file with a FileDialog
     */
    public void openLoadFile() {
        FileDialog dialog = new FileDialog((Dialog) null, "Load experience", FileDialog.LOAD);
        dialog.setVisible(true);
        String directoryName = dialog.getDirectory();
        String fileName = dialog.getFile();
        String filePath = directoryName + fileName;
        showExperienceFileThreadingInfo();
        
        try {
            nar.addInputFile(filePath);
            //nar.addInput(new TextInput(new File(filePath)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Initialize the system for a new finish
     */
    public void init() {
        setSpeed(0);
        setSpeed(0);        //call twice to make it start as paused
        updateGUI();
        nar.memory.event.on(CyclesEnd.class, this);
    }

    final Runnable updateGUIRunnable = new Runnable() {
        @Override public void run() {
            updateGUI();
        }
    };
    
    /** in ms */
    long lastUpdateTime = -1;
    
    /** in memory cycles */
    
    protected void updateGUI() {
                
        speedSlider.repaint();

    }
    

    @Override
    public void event(final Class event, final Object... arguments) {
        if (event == CyclesEnd.class) {
            
            long now = System.currentTimeMillis();
            long deltaTime = now - lastUpdateTime;
            
            if ((deltaTime >= GUIUpdatePeriodMS) /*|| (!updateScheduled.get())*/) {              
                                                
                speedSlider.repaint();
                
                SwingUtilities.invokeLater(updateGUIRunnable);

                lastUpdateTime = now;
                
            }
        }
    }

    
    /**
     * Handling button click
     *
     * @param e The ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj instanceof JButton) {
            if (obj == stopButton) {
                setSpeed(0);
                updateGUI();
            } else if (obj == walkButton) {
                nar.stop();
                nar.cycles(1);
                updateGUI();
            }
        } else if (obj instanceof JMenuItem) {
            String label = e.getActionCommand();
            switch (label) {
                //case "Enable Full Internal Experience":
                    //fullInternalExp.setEnabled(false);
                    //Parameters.INTERNAL_EXPERIENCE_FULL=true;
                    //Parameters.ENABLE_EXPERIMENTAL_NARS_PLUS=!Parameters.ENABLE_EXPERIMENTAL_NARS_PLUS;
                  //  break;
                    
//                case "Enable NARS+ Ideas":
//                    narsPlusItem.setEnabled(false);
//                    nar.memory.param.experimentalNarsPlus.set(true);
//                    break;
//                case "Enable Internal Experience (NAL9)":
//                    internalExperienceItem.setEnabled(false);
//                    nar.memory.param.internalExperience.set(true);
//                    break;
                     
                case "Save Memory":
                {
                    try {
                        FileDialog dialog = new FileDialog((Dialog) null, "Save memory", FileDialog.SAVE);
                        dialog.setVisible(true);
                        String directoryName = dialog.getDirectory();
                        String fileName = dialog.getFile();
                        String path = directoryName + fileName;
                        nar.SaveToFile(path);
                    } catch (IOException ex) {
                        Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                    break;
                case "Load Memory":
                {
                    try {
                        FileDialog dialog = new FileDialog((Dialog) null, "Load memory", FileDialog.LOAD);
                        dialog.setVisible(true);
                        String directoryName = dialog.getDirectory();
                        String fileName = dialog.getFile();
                        String filePath = directoryName + fileName;
                        Nar loadedNAR = Nar.LoadFromFile(filePath);
                        new NARSwing(loadedNAR);
                        loadedNAR.memory.emit(OutputHandler.ECHO.class, "Memory file " + fileName + " loaded successfully.");
                        //new javax.swing.JOptionPane.showInputDialog(new javax.swing.JFrame(), "Memory loaded");
                        parent.mainWindow.setVisible(false);
                        parent.mainWindow.dispose();
                    } catch (IOException ex) {
                        Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                    Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
                }
                    break;
                } 
                case "Load Experience":
                    openLoadFile();
                    break;
                case "Save Experience":
                    if (savingExp) {
                        experienceWriter.closeSaveFile();
                    } else {
                        showExperienceFileThreadingInfo();
                        FileDialog dialog = new FileDialog((Dialog) null, "Save experience", FileDialog.SAVE);
                        dialog.setVisible(true);
                        String directoryName = dialog.getDirectory();
                        String fileName = dialog.getFile();
                        String path = directoryName + fileName;
                        experienceWriter.openSaveFile(path);
                    }
                    savingExp = !savingExp;
                    break;
                case "Reset":
                    /// TODO mixture of modifier and reporting
                    //narsPlusItem.setEnabled(true);
                    //internalExperienceItem.setEnabled(true);
                    nar.reset();
                    break;
                case "Related Information":
                    new MessageDialog(Nar.WEBSITE);
                    break;
                case "About NARS":
                    new MessageDialog(Nar.NAME + " " + Nar.VERSION+"\n\n"+ Nar.WEBSITE);
                    break;
            }
        }
    }


    
    private NSlider newSpeedSlider() {
            final StringBuilder sb = new StringBuilder(32);

        final NSlider s = new NSlider(0f, 0f, 1.0f) {

            
            @Override
            public String getText() {
                if (value == null) {
                    return "";
                }
                
                if (sb.length() > 0) sb.setLength(0);

                sb.append(nar.time());      
                

                if (currentSpeed == 0) {
                    sb.append(" - pause");
                } else if (currentSpeed == 1.0) {
                    sb.append(" - max speed");
                } else {
                    sb.append(" - ").append(nar.getMinCyclePeriodMS()).append(" ms/step");
                }
                return sb.toString();
            }

            @Override
            public void onChange(float v) {                
                setSpeed(v);
            }

        };
        this.speedSlider = s;

        return s;
    }

    private NSlider newThreadsSlider() {
        final NSlider s = this.threadSlider = new NSlider(1f, 1f, 16f) {

            @Override
            public String getText() {
                if (value == null) {
                    return "";
                }

                float v = value();
                String s = "Threads:" + super.getText();
                return s;
            }

            @Override
            public void setValue(float v) {
                super.setValue(Math.round(v));
                repaint(); //needed to update when called from outside, as the 'focus' button does
            }

            @Override
            public void onChange(float v) {
                int level = (int) v;
                long speed = nar.getMinCyclePeriodMS();
                boolean wasRunning = nar.isRunning();
                nar.stop();
                (nar.narParameters).THREADS_AMOUNT = level;
                if(wasRunning) {
                    nar.start(speed);
                }
            }

        };

        return s;
    }
    
    private NSlider newDecisionThresholdSlider() {
        final NSlider s = this.decisionThresholdSlider = new NSlider(0, 0.0f, 1.0f) {

            @Override
            public String getText() {
                if (value == null) {
                    return "";
                }

                float v = value();
                String s = "Decision Thrs." + super.getText();
                return s;
            }

            @Override
            public void setValue(float v) {
                super.setValue(v);
                repaint(); //needed to update when called from outside, as the 'focus' button does
            }

            @Override
            public void onChange(float v) {
                (nar.narParameters).DECISION_THRESHOLD = v;
            }
        };

        return s;
    }
    
    private NSlider newVolumeSlider() {
        final NSlider s = this.volumeSlider = new NSlider(100f, 0, 100f) {

            @Override
            public String getText() {
                if (value == null) {
                    return "";
                }

                float v = value();
                String s = "Volume:" + super.getText() + " (";

                if (v == 0) {
                    s += "Silent";
                } else if (v < 25) {
                    s += "Quiet";
                } else if (v < 75) {
                    s += "Normal";
                } else {
                    s += "Loud";
                }

                s += ")";
                return s;
            }

            @Override
            public void setValue(float v) {
                super.setValue(Math.round(v));
                repaint(); //needed to update when called from outside, as the 'focus' button does
            }

            @Override
            public void onChange(float v) {
                int level = (int) v;
                (nar.narParameters).VOLUME = level;
            }

        };

        return s;
    }

    public void setSpeed(float nextSpeed) {
        final float maxPeriodMS = 1024.0f;

        if (nextSpeed == 0) {
            if (currentSpeed == 0) {
                if (lastSpeed == 0) {
                    lastSpeed = defaultSpeed;
                }
                nextSpeed = lastSpeed;
            } else {
            }

        }
        if (currentSpeed == nextSpeed) return;
        lastSpeed = currentSpeed;
        speedSlider.repaint();
        stopButton.setText(String.valueOf(FA_PlayCharacter));

        /*if (currentSpeed == s)
         return;*/
        speedSlider.setValue(nextSpeed);
        currentSpeed = nextSpeed;

        float logScale = 50f;
        if (nextSpeed > 0) {
            long ms = (long) ((1.0 - Math.log(1+nextSpeed*logScale)/Math.log(1+logScale)) * maxPeriodMS);
            if (ms < 1) {
                if (allowFullSpeed)
                    ms = 0;
                else
                    ms = 1;
            }
            stopButton.setText(String.valueOf(FA_StopCharacter));
            //nar.setThreadYield(true);
            nar.start(ms); //nar.getCyclesPerFrame()
        } else {
            stopButton.setText(String.valueOf(FA_PlayCharacter));
            nar.stop();
        }
    } 
    
    //http://astronautweb.co/snippet/font-awesome/
    private final char FA_PlayCharacter = '\uf04b';
    private final char FA_StopCharacter = '\uf04c';
    private final char FA_FocusCharacter = '\uf11e';
    private final char FA_ControlCharacter = '\uf085';

    private JComponent newParameterPanel() {
        JPanel p = new JPanel();

        JPanel pc = new JPanel();

        pc.setLayout(new GridLayout(1, 0));

        stopButton = new AwesomeButton(FA_StopCharacter);
        stopButton.setBackground(Color.DARK_GRAY);
        stopButton.addActionListener(this);
        pc.add(stopButton);

        walkButton = new AwesomeButton('\uf051');
        walkButton.setBackground(Color.DARK_GRAY);
        walkButton.setToolTipText("Walk 1 Cycle");
        walkButton.addActionListener(this);
        pc.add(walkButton);

        JButton focusButton = new AwesomeButton(FA_FocusCharacter);
        focusButton.setBackground(Color.DARK_GRAY);
        focusButton.setToolTipText("Focus");
        focusButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setSpeed(1.0f);
                volumeSlider.setValue(0.0f);
                volumeSlider.repaint();
            }

        });
        pc.add(focusButton);
        
        
        JButton pluginsButton = new AwesomeButton(FA_ControlCharacter);
        pluginsButton.setToolTipText("Plugins");
        pluginsButton.addActionListener(new ActionListener() {

            @Override public void actionPerformed(ActionEvent e) {
                new NWindow("Plugins", new PluginPanel(nar)).show(350, 600);
            }

        });
        pc.add(pluginsButton);
        
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.ipady = 8;

        p.add(pc, c);
        
        NSlider vs = newVolumeSlider();
        vs.setFont(vs.getFont().deriveFont(Font.BOLD));
        p.add(vs, c);

        NSlider ss = newSpeedSlider();
        ss.setFont(vs.getFont());
        p.add(ss, c);

        NSlider vs2 = newThreadsSlider();
        vs2.setFont(vs.getFont());
        p.add(vs2, c);
        
        NSlider vs3 = newDecisionThresholdSlider();
        vs3.setFont(vs.getFont());
        p.add(vs3, c);
        
        c.ipady = 4;

        // FIXME< we need to have a slider which uses the float value >
        //p.add(new NSlider(nar.narParameters.DECISION_THRESHOLD, "Decision Threshold", 0.0f, 1.0f), c);
        //p.add(new NSlider(memory.param.projectionDecay, "Projection Decay", 0.0f, 1.0f), c);
        //p.add(new NSlider(memory.param.taskLinkForgetDurations, "Task Duration", 0.0f, 20), c);
        //p.add(new NSlider(memory.param.termLinkForgetDurations, "Belief Duration", 0.0f, 20), c);
        //p.add(new NSlider(memory.param.conceptForgetDurations, "Concept Duration", 0.0f, 20), c);
        //p.add(new NSlider(memory.param.eventForgetDurations, "Event Duration", 0.0f, 20), c);

        
//
//        //JPanel chartPanel = new JPanel(new GridLayout(0,1));
//        {
//            this.chart = new MeterVis(senses, chartHistoryLength);
//            //chartPanel.add(chart);
//                        
//        }
//        
//        c.weighty = 1.0;
//        c.fill = GridBagConstraints.BOTH;        
//        //p.add(new JScrollPane(chartPanel), c);
//        p.add(chart, c);

        /*c.fill = c.BOTH;
        p.add(Box.createVerticalBox(), c);*/
        

        return p;
    }

    private NSlider newIntSlider(final PortableInteger x, final String prefix, int min, int max) {
        final NSlider s = new NSlider(x.intValue(), min, max) {

            @Override
            public String getText() {
                return prefix + ": " + super.getText();
            }

            @Override
            public void setValue(float v) {
                int i = Math.round(v);
                super.setValue(i);
                x.set(i);
            }

            @Override
            public void onChange(float v) {
            }
        };

        return s;
    }

    /** if true, then the speed control allows Nar to run() each iteration with 0 delay.
     *  otherwise, the minimum delay is 1ms */
    public void setAllowFullSpeed(boolean allowFullSpeed) {
        this.allowFullSpeed = allowFullSpeed;
    }
}
