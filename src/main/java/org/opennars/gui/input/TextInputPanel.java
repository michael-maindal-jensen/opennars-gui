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
package org.opennars.gui.input;

import automenta.vivisect.swing.NPanel;
import automenta.vivisect.swing.NWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.opennars.io.events.OutputHandler;
import org.opennars.main.Nar;
import org.opennars.gui.FileTreeModel;

import static org.opennars.gui.output.SwingLogPanel.setConsoleFont;
import org.opennars.io.ConfigReader;
import org.opennars.io.events.OutputHandler.OUT;
import org.opennars.main.Debug;


public class TextInputPanel extends NPanel /*implements ActionListener*/ {
    private ReactionPanel infoPane;
    private final JMenuBar menu;
    private JSplitPane mainSplit;
    private JButton defaultButton;

    public interface InputAction {
        String getLabel();
        
        /** may be null */
        String getDescription();
        
        /** perform the action; the returned String, if not null, replaces the current
         * input allowing for actions to transform the input
         */
        String run();
    
        /** between 0..1 used for sorting and affecting displayed prominence of menu option */
        double getStrength();

    }
    
    /** each InputMode consists of:
     *      Interpretation - attempts to describe its interpretation (or lack of) of what is currently entered
     *      Actions - possible actions the current input enables, each with description. more than one may be invoked (checkboxes)
     *      Completions - descriptions of possible ways to complete the current input, and their meaning
     * 
     */
    public interface TextInputMode /* extends AbstractInputMode */ {
        
        void setInputState(Nar nar, String input  /* int cursorPosition */);
        
        /** null if none available */
        String getInterpretation();
        
        void getActions(List<InputAction> actionsCollected);
        
    }
    
    /** provides actions that will be available even if, or only if, input is blank */
    public class NullInput implements TextInputMode {

        private String input;
        private Nar nar;

        public final InputAction clear = new InputAction() {
            @Override public String getLabel() {
                return "Clear";
            }

            @Override public String getDescription() {
                return "Empty the input area";
            }

            @Override public String run() {
                return "";
            }                

            @Override
            public double getStrength() {
                return 0.25;
            }            
        };
        public final InputAction library = new InputAction() {
            private JTree fileTree = null;
            
            @Override public String getLabel() {
                return "Library";
            }

            @Override public String getDescription() {
                return "Browse the file library for experiences to load";
            }

            @Override public String run() {
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                File file = null;
                file = new File("./nal/");
                TreeModel model = new FileTreeModel(file);
                /*if (fileTree==null)*/ {
                    fileTree = new JTree(model);
                    fileTree.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            int selRow = fileTree.getRowForLocation(e.getX(), e.getY());
                            TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
                            if (selRow != -1) {
                                if (e.getClickCount() == 1) {
                                } else if (e.getClickCount() == 2) {
                                    //DoubleClick
                                    File f = (File) selPath.getLastPathComponent();

                                    if (!f.isDirectory()) {
                                        try {
                                            nar.addInputFile(f.getAbsolutePath());
                                            nar.emit(OUT.class, "Loaded file: " + f.getAbsolutePath());
                                        } catch (Exception ex) {
                                            System.err.println(ex);
                                        }
                                    }
                                }
                            }
                        }
                    });
                    new NWindow("Experience Library", new JScrollPane(fileTree)).show(400, 200);
                }

                return "";
            }                

            @Override
            public double getStrength() {                
                return (input.length() == 0) ? 0.5 : 0.15;
            }
            
        };

        
        @Override
        public void setInputState(Nar nar, String input) {
            this.input = input;
            this.nar = nar;
        }

        @Override
        public String getInterpretation() {
            return null;
        }

        @Override
        public void getActions(List<InputAction> actionsCollected) {
            if (input.length() > 0) {
                actionsCollected.add(clear);
                //TODO concept search
                //TODO operator search
                
            }
            actionsCollected.add(library);
        }
    }
    
    public class NarseseInput implements TextInputMode {
        
        private String input;
        private Nar nar;

        @Override
        public void setInputState(Nar nar, String input) {
            this.input = input;
            this.nar = nar;
        }

        @Override
        public String getInterpretation() {
            return "";
        }

        public InputAction inputDirect = new InputAction() {

            @Override
            public String getLabel() {
                return "Input";
            }

            @Override
            public String getDescription() {
                return "Direct input into Nar";
            }

            @Override
            public String run() {
                return evaluateSeq(input);
            }

            @Override
            public double getStrength() {
                return 1.5;
            }
                
        };
        
        public InputAction step = new InputAction() {

            @Override
            public String getLabel() {
                return "Step";
            }

            @Override
            public String getDescription() {
                return "Compute 1 cycle";
            }

            @Override
            public String run() {
                nar.cycles(1);
                return input;
            }

            @Override
            public double getStrength() {
                return 1.0;
            }
                
        };
        
        @Override
        public void getActions(List<InputAction> actionsCollected) {
            //TODO only allow input if it parses, but while parser is incomplete, allow all
            if (input.length() > 0)
                actionsCollected.add(inputDirect);
            
            //actionsCollected.add(step);
            //TODO reset
            
            
            /*
            Other Actions:
                Ask - direct input a question, and create a solution window to watch for answers
                Command - direct input a goal, and create a task window to watch progress
                Parse Tree - show the parse tree for input (but don't clear it)
            */
        }
        
        public String evaluateSeq(String input) {
            //TODO make sequential evaluation
            try{
                nar.addInput(input);
            }
            catch(Exception ex) {
                if(Debug.DETAILED) {
                    throw new IllegalStateException("error parsing:" + input, ex);
                }

                nar.memory.emit(OutputHandler.ECHO.class, "Parsing failed!");

                return input;
            }
            nar.cycles(1);
            return "";
        }
    }
    private final Nar nar;

    /**
     * Input area
     */
    private JTextArea inputText;

    private final JPanel centerPanel;
    private final JComponent textInput;
    
    public List<TextInputMode> modes = new ArrayList();
    

    /**
     * Constructor
     *
     * @param nar The reasoner
     */
    public TextInputPanel(final Nar nar) {
        super(new BorderLayout());

        this.nar = nar;

        modes.add(new NarseseInput());
        //modes.add(new EnglishInput());
        modes.add(new NullInput());
        
        centerPanel = new JPanel(new BorderLayout());

        menu = new JMenuBar();

        menu.setOpaque(false);
        menu.setBackground(Color.BLACK);
        setBackground(Color.BLACK);

        textInput = newTextInput();
        centerPanel.add(textInput);

        add(centerPanel, BorderLayout.CENTER);
        add(menu, BorderLayout.SOUTH);
        menu.setVisible(true);

    }

    public class ReactionPanel extends JPanel {

        /**
         * List with buttons (instant invoke) and checkboxes with 'All' at the top when two or more are selected
         */
        Date date = new Date();
        public ReactionPanel() {
            super(new BorderLayout());
        }

        public void update() {

            List<String[]> interpretations = new ArrayList();
            List<InputAction> actions = new ArrayList();
            
            String input = inputText.getText();
            
            for (final TextInputMode t : modes) {
                t.setInputState(nar, input);
                
                String interp = t.getInterpretation();
                if (interp!=null) {
                    interpretations.add(new String[] { t.getClass().getSimpleName(), interp } );
                }
                
                t.getActions(actions);                
            }

            menu.removeAll();

            defaultButton = null;
            double maxStrength = 0;
            for (InputAction a : actions) {
                JButton b = new JButton(a.getLabel());
                
                double strength = a.getStrength();
                if (strength > maxStrength) {
                    defaultButton = b;
                    maxStrength = strength;
                }
                // b.setFont(b.getFont().deriveFont((float)(b.getFont().getSize() * (0.5f + 0.5f * strength))));
                b.setForeground(Color.WHITE); 
                b.setBackground(Color.DARK_GRAY);
                b.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                //TODO catch error around run() ?
                                String result = a.run();
                                if (result!=null) {
                                    inputText.setText(result);
                                }
                            }
                        });
                    }
                });
               
                menu.add(b);
            }

            menu.validate();
            menu.repaint();
            
            validate();
            repaint();
            date = new Date();
        }

    }

    // TODO move this to its own class
    public JComponent newTextInput() {
        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);        
        
        infoPane = new ReactionPanel();

        inputText = new JTextArea("");
        inputText.setRows(3);

        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent documentEvent) {
                updated(documentEvent);
            }

            public void insertUpdate(DocumentEvent documentEvent) {
                updated(documentEvent);
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                updated(documentEvent);
            }

            private void updated(DocumentEvent documentEvent) {
                updateContext();
            }
        };
        inputText.getDocument().addDocumentListener(documentListener);

        inputText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                //control-enter evaluates
                if (e.isControlDown()) {
                    if (e.getKeyCode() == 10) {
                        runDefault();
                    }
                }
            }
        });
        setConsoleFont(inputText, 20);

        mainSplit.add(new JScrollPane(inputText), 0);

        infoPane.setVisible(false);
        mainSplit.add(infoPane, 1);

        updateContext();
        
        return mainSplit;
    }

    protected void updateContext() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                infoPane.update();
            }            
        });
    }

    /**
     * Initialize the window
     */
    public void init() {
        inputText.setText("");
    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing) {

        } else {

        }
    }
    
    protected void runDefault() {
        if (defaultButton!=null)
            defaultButton.doClick();
    }

    private void close() {
        setVisible(false);
    }
}
