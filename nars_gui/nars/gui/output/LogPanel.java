package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.gui.InferenceLogger;
import nars.gui.InferenceLogger.LogOutput;
import nars.gui.NARControls;
import nars.gui.FAButton;
import nars.gui.AwesomeToggleButton;
import nars.gui.NPanel;
import nars.gui.NSlider;
import nars.gui.WrapLayout;
import nars.io.Output;
import nars.io.TextOutput;

abstract public class LogPanel extends NPanel implements Output, LogOutput {

    static String getText(Object o, boolean showStamp, NAR nar) {
        return TextOutput.getOutputString(o, showStamp, nar);
    }



    protected final NAR nar;
    public static final int maxIOTextSize = (int) 1E5;
    protected boolean showErrors = true;
    protected boolean showStamp = false;
    protected boolean showQuestions = true;
    protected boolean showStatements = true;

    /**
     * the log file
     */
    protected PrintWriter logFile = null;

    private final InferenceLogger logger;
    private String logFilePath;

    public LogPanel(NARControls c) {
        super();
        setLayout(new BorderLayout());

        this.nar = c.nar;
        this.logger = c.logger;

        //JPanel menuBottom = new JPanel(new WrapLayout(FlowLayout.RIGHT, 0, 0));
        JPanel menuTop = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, 0));

        //menuBottom.setOpaque(false);
        //menuBottom.setBorder(new EmptyBorder(0,0,0,0));
        menuTop.setOpaque(false);
        menuTop.setBorder(new EmptyBorder(0, 0, 0, 0));

        
        JButton clearButton = new FAButton('\uf016');
        clearButton.setToolTipText("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearLog();
            }
        });
        menuTop.add(clearButton);

        final String defaultStreamButtonLabel = "Stream to File..";
        final JToggleButton streamButton = new AwesomeToggleButton('\uf0c7', '\uf052');
        streamButton.setToolTipText(defaultStreamButtonLabel);
        streamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (streamButton.isSelected()) {
                    if (!openLogFile()) {
                        streamButton.setSelected(false);
                    } else {
                        streamButton.setToolTipText("Streaming...");
                    }
                } else {
                    streamButton.setToolTipText(defaultStreamButtonLabel);
                    closeLogFile();
                }
            }
        });
        menuTop.add(streamButton);

        menuTop.add(Box.createHorizontalStrut(4));

        final JToggleButton showStatementsBox = new JToggleButton("Statements");
        showStatementsBox.setSelected(showStatements);
        showStatementsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatements = showStatementsBox.isSelected();
            }
        });
        menuTop.add(showStatementsBox);

        final JToggleButton showQuestionsBox = new JToggleButton("Questions");
        showQuestionsBox.setSelected(showQuestions);
        showQuestionsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showQuestions = showQuestionsBox.isSelected();
            }
        });
        menuTop.add(showQuestionsBox);

        final JToggleButton showErrorBox = new JToggleButton("Errors");
        showErrorBox.setSelected(showErrors);
        showErrorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showErrors = showErrorBox.isSelected();
            }
        });
        menuTop.add(showErrorBox);

        final JToggleButton showStampBox = new JToggleButton("Stamps");
        showStampBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStamp = showStampBox.isSelected();
            }
        });
        menuTop.add(showStampBox);

        final JToggleButton showTraceBox = new JToggleButton("Trace");
        showTraceBox.setEnabled(true);
        showTraceBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTrace(showTraceBox.isSelected());
            }
        });
        setTrace(showTraceBox.isSelected());
        menuTop.add(showTraceBox);

        menuTop.add(Box.createHorizontalStrut(4));

        final NSlider fontSlider = new NSlider(12 /*ioText.getFont().getSize()*/, 6, 40) {
            @Override
            public void onChange(double v) {
                setFontSize(v);
            }
        };
        fontSlider.setPrefix("Font size: ");
        menuTop.add(fontSlider);

        //add(menuBottom, BorderLayout.SOUTH);
        add(menuTop, BorderLayout.NORTH);

        addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
            }
        });

    }
    
    abstract protected void setFontSize(double v);
    abstract protected void clearLog();

    @Override
    protected void onShowing(boolean showing) {
        if (showing) {
            nar.addOutput(this);
        } else {
            nar.removeOutput(this);
        }
    }

    @Override
    public void output(final Class c, Object o) {

        if ((!showErrors) && (c == ERR.class)) {
            return;
        }
        if (o instanceof Sentence) {
            Sentence s = (Sentence) o;

            if (s.isQuestion() && !showQuestions) {
                return;
            }
            if (s.isJudgment() && !showStatements) {
                return;
            }
        }

        print(c, o);

    }
    
    abstract void print(Class c, Object o);
    
    abstract void limitBuffer(int incomingDataSize);


    public static Color getChannelColor(Class c) {
        
        switch (c.getSimpleName()) {
            case "OUT":                
                return Color.GREEN;
            case "IN":
                return Color.YELLOW;
            case "ERR":
                return Color.ORANGE;
        }
        
        return Color.GRAY;
    }

    static Color getPriorityColor(float priority) {
        return new Color(priority, priority, priority);
    }
    static Color getFrequencyColor(float frequency) {
        return new Color(1.0f - frequency, frequency, 0);
    }
    static Color getConfidenceColor(float confidence) {
        return new Color(0,0,confidence);
    }

    static Color getStatementColor(char punctuation, float priority) {
        
        float r = 1f, g = 1f, b = 1f;
        switch (punctuation) {
            case '!': r = 1f; g = 0.75f; b = 0f; break;
            case '?': b = 1f; r = 0.3f; g = 0f; break;
            case '.': break;
        }        
        r *= 0.25f + 0.75f*priority;
        g *= 0.25f + 0.75f*priority;
        b *= 0.25f + 0.75f*priority;
        return new Color(r, g, b);
    }
    

    public static final class LOG {
    }

    @Override
    public void logAppend(String s) {
        output(LOG.class, s);
    }

    public void setTrace(boolean b) {
        if (b) {
            logger.addOutput(this);
        } else {
            logger.removeOutput(this);
        }
    }

    public boolean openLogFile() {
        FileDialog dialog = new FileDialog((Dialog) null, "Inference Log", FileDialog.SAVE);

        dialog.setVisible(true);
        String directoryName = dialog.getDirectory();
        logFilePath = dialog.getFile();
        if (logFilePath == null) {
            return false;
        }

        try {
            boolean append = true;
            boolean autoflush = true;
            logFile = new PrintWriter(new FileWriter(directoryName + logFilePath, append), autoflush);
            output(LOG.class, "Stream opened: " + logFilePath);
            return true;
        } catch (IOException ex) {
            output(ERR.class, "Log file save: I/O error: " + ex.getMessage());
        }

        return false;
    }

    public void closeLogFile() {
        if (logFile != null) {
            output(LOG.class, "Stream saved: " + logFilePath);
            logFile.close();
            logFile = null;
        }
    }

 
}
